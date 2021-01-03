package com.learn.gmall.manage.service.impl;

import com.google.gson.Gson;
import com.learn.gmall.api.api.SkuService;
import com.learn.gmall.api.bean.*;
import com.learn.gmall.manage.component.PmsSkuInfoSender;
import com.learn.gmall.manage.mapper.*;
import com.learn.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.learn.gmall.manage.constant.ConstantsIfc.WAIT_TO_SENT;

@DubboService
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PmsSkuInfoSender pmsSkuInfoSender;

    @Autowired
    private PmsSkuMessageLogMapper pmsSkuMessageLogMapper;

    @Override
    @Transactional
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        Integer skuInfoId = pmsSkuInfo.getId();

        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        skuAttrValueList.stream().forEach(skuAttrValue -> {
            skuAttrValue.setSkuId(skuInfoId);
            pmsSkuAttrValueMapper.insertSelective(skuAttrValue);
        });

        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        skuSaleAttrValueList.stream().forEach(skuSaleAttrValue -> {
            skuSaleAttrValue.setSkuId(skuInfoId);
            pmsSkuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        });

        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        skuImageList.stream().forEach(skuImage -> {
            skuImage.setSkuId(skuInfoId);
            pmsSkuImageMapper.insertSelective(skuImage);
        });

        LocalDateTime now = LocalDateTime.now();
        PmsSkuMessageLog pmsSkuMessageLog = PmsSkuMessageLog.builder()
                .skuId(skuInfoId)
                .createTime(now)
                .updateTime(now)
                .status(WAIT_TO_SENT)
                .retryCount(0)
                .build();
        pmsSkuMessageLogMapper.insertSelective(pmsSkuMessageLog);
        pmsSkuInfoSender.sendPmsSkuInfo(pmsSkuInfo);

    }

    @Override
    public PmsSkuInfo getPmsSkuInfoById(Integer skuId) {
        Gson gson = new Gson();
        PmsSkuInfo pmsSkuInfo;
        try (Jedis jedis = redisUtil.getJedis()) {
            String pmsSkuInfoInStr = jedis.get("sku:" + skuId + ":skuInfo");
            //redis不为空, 直接返回, redis中值为空, 从db中获取
            if (StringUtils.isNotBlank(pmsSkuInfoInStr)) {
                pmsSkuInfo = gson.fromJson(pmsSkuInfoInStr, PmsSkuInfo.class);
            } else {
                //从db中获取
                //设置分布式锁
                String lockValue = UUID.randomUUID().toString();
                String lockResult = jedis.set("lock:" + skuId + ":redislock", lockValue, "nx", "px", 60 * 1000);
                if ("OK".equals(lockResult)) {
                    System.out.println("线程" + Thread.currentThread().getName() + "获取分布式锁成功, 从db中获取数据");
                    pmsSkuInfo = getPmsSkuInfoByIdFromDb(skuId);
                    if (pmsSkuInfo != null) {
                        jedis.setex("sku:" + skuId + ":skuInfo", 600, gson.toJson(pmsSkuInfo));
                    } else {
                        //db中不存在, 设置一个空值, 避免缓存穿透
                        jedis.setex("sku:" + skuId + ":skuInfo", 60, gson.toJson(PmsSkuInfo.builder().build()));
                    }
                    //释放分布式锁
                    String scripts = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del',KEYS[1]) else return 0 end";
                    jedis.eval(scripts, Collections.singletonList("lock:" + skuId + ":redislock"), Collections.singletonList(lockResult));
                    System.out.println("线程" + Thread.currentThread().getName() + "成功释放分布式锁");
                }
                System.err.println("线程" + Thread.currentThread().getName() + "获取分布式锁失败, 从cache中获取数据");
                return getPmsSkuInfoById(skuId);
            }
        }
        return pmsSkuInfo;
    }

    private PmsSkuInfo getPmsSkuInfoByIdFromDb(Integer skuId) {
        return pmsSkuInfoMapper.selectPmsSkuInfoByPrimaryKey(skuId);
    }

    @Override
    public List<PmsSkuInfo> getPmsSkuInfoListByProductId(Integer spuId) {
        return pmsSkuInfoMapper.selectPmsSkuInfoListByProductId(spuId);
    }

    @Override
    public BigDecimal checkPrice(Integer productSkuId) {
        PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectOne(PmsSkuInfo.builder().id(productSkuId).build());
        if (pmsSkuInfo != null && pmsSkuInfo.getPrice() != null) {
            return pmsSkuInfo.getPrice();
        }
        return new BigDecimal("0");
    }
}
