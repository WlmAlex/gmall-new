package com.learn.gmall.cart.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.learn.gmall.api.api.CartService;
import com.learn.gmall.api.bean.OmsCartItem;
import com.learn.gmall.api.bean.OmsOrderItem;
import com.learn.gmall.cart.mapper.OmsCartItemMapper;
import com.learn.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.learn.gmall.cart.constant.ConstantIfc.cart_key;
import static com.learn.gmall.cart.constant.ConstantIfc.distributed_lock;

@DubboService
public class CartServiceImpl implements CartService {

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;

    @Autowired
    private RedisUtil redisUtil;

    private final Gson gson = new Gson();

    @Override
    public List<OmsCartItem> getCartListByMemberId(String memberId) {
        List<OmsCartItem> omsCartItemList;
        try (Jedis jedis = redisUtil.getJedis()) {
            String cartKey = cart_key.replace("memberId", memberId);
            String cartListValue = jedis.get(cartKey);
            if (StringUtils.isNotBlank(cartListValue)) {
                omsCartItemList = gson.fromJson(cartListValue, new TypeToken<List<OmsCartItem>>() {
                }.getType());
            } else {
                //从db中查找, 设置分布式锁
                String uniqueValue = UUID.randomUUID().toString();
                String lockKey = distributed_lock.replace("memberId", memberId);
                String lockResult = jedis.set(lockKey, uniqueValue, "nx", "px", 60 * 1000);
                if ("OK".equals(lockResult)) {
                    //获取分布式锁成功, 从db中查询数据
                    omsCartItemList = getCartListFromDb(memberId);
                    if (!CollectionUtils.isEmpty(omsCartItemList)) {
                        jedis.setex(cartKey, 600, gson.toJson(omsCartItemList));
                    } else {
                        //集合为空, 避免缓存穿透, 设置空串
                        jedis.setex(cartKey, 60, gson.toJson(""));
                    }

                    //使用lua脚本, 释放redis锁
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(uniqueValue));
                }
                //获取分布式锁失败, 重试, 从缓存中查询结果
                return getCartListByMemberId(memberId);
            }
        }
        return omsCartItemList;
    }

    private List<OmsCartItem> getCartListFromDb(String memberId) {
        return omsCartItemMapper.select(OmsCartItem.builder().memberId(memberId).build());
    }

    @Override
    @Transactional
    public void updateCartList(List<OmsCartItem> omsCartItemList) {
        Optional.ofNullable(omsCartItemList).ifPresent(omsCartItems -> omsCartItems.stream()
                .forEach(omsCartItem -> omsCartItemMapper.updateByPrimaryKeySelective(omsCartItem)));
        String memberId = omsCartItemList.get(0).getMemberId();
        try (Jedis jedis = redisUtil.getJedis()) {
            String cartKey = cart_key.replace("memberId", memberId);
            jedis.set(cartKey, gson.toJson(omsCartItemList));
        }
    }

    @Override
    public void addToCart(OmsCartItem omsCartItem) {
        try {
            omsCartItemMapper.insertSelective(omsCartItem);

        } catch (Exception e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                //ignore
            }
        }
    }

    @Override
    public void updateCartItemStatus(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria()
                .andEqualTo("memberId", omsCartItem.getMemberId())
                .andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExample(omsCartItem, example);
    }

    @Override
    public BigDecimal checkQuantity(Integer productSkuId, String memberId) {
        OmsCartItem omsCartItem = omsCartItemMapper.selectOne(OmsCartItem.builder().memberId(memberId).productSkuId(productSkuId).build());
        if (omsCartItem != null && omsCartItem.getQuantity() != null) {
            return omsCartItem.getQuantity();
        }
        return new BigDecimal("0");
    }

    @Override
    public void clearCart(OmsOrderItem omsOrderItem) {
        Integer productSkuId = omsOrderItem.getProductSkuId();
        BigDecimal productQuantity = omsOrderItem.getProductQuantity();
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("productSkuId", productSkuId).andEqualTo("quantity", productQuantity);
        OmsCartItem omsCartItem = omsCartItemMapper.selectOneByExample(example);

        try (Jedis jedis = redisUtil.getJedis()) {
            String cartKey = cart_key.replace("memberId", omsCartItem.getMemberId());
            jedis.del(cartKey);
        }
        omsCartItemMapper.delete(omsCartItem);
    }

}