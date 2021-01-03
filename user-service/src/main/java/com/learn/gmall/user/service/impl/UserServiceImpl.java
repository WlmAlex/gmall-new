package com.learn.gmall.user.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.learn.gmall.api.api.UserService;
import com.learn.gmall.api.bean.UmsMember;
import com.learn.gmall.api.bean.UmsMemberReceiveAddress;
import com.learn.gmall.user.mapper.UmsMemberMapper;
import com.learn.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.learn.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

import static com.learn.gmall.user.constant.ConstantIfc.*;

@DubboService
public class UserServiceImpl implements UserService {

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    private final Gson gson = new Gson();

    @Override
    public UmsMember login(UmsMember umsMember) {
        try (Jedis jedis = redisUtil.getJedis()) {
            String userInfo = jedis.get(String.format(user_info_key, umsMember.getUsername(), umsMember.getPassword()));
            if (StringUtils.isNotBlank(userInfo)) {
                UmsMember umsMemberFromCache = gson.fromJson(userInfo, UmsMember.class);
                return umsMemberFromCache;
            }

            UmsMember umsMemberFromDb = umsMemberMapper.selectOne(umsMember);
            if (umsMemberFromDb != null) {
                jedis.setex(String.format(user_info_key, umsMember.getUsername(), umsMember.getPassword()), 60 * 60 * 2, gson.toJson(umsMemberFromDb));
            }
            return umsMemberFromDb;
        }
    }

    @Override
    public void addUserToken(String token, String id) {
        try (Jedis jedis = redisUtil.getJedis()) {
            jedis.setex(user_token_key.replace("memberId", id), 60 * 60 * 2, token);
        }
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        try (Jedis jedis = redisUtil.getJedis()) {
            String receive_addr_key = String.format(user_receive_address_key, memberId);
            String receiveAddressStr = jedis.get(receive_addr_key);
            if (StringUtils.isNotBlank(receiveAddressStr)) {
                List<UmsMemberReceiveAddress> receiveAddressList = gson.fromJson(receiveAddressStr, new TypeToken<List<UmsMemberReceiveAddress>>() {
                }.getType());
                return receiveAddressList;
            }

            //Corresponding receive addresses doesn't exist in cache, find it from db
            List<UmsMemberReceiveAddress> receiveAddressList = umsMemberReceiveAddressMapper.select(UmsMemberReceiveAddress.builder().memberId(memberId).build());
            if (!CollectionUtils.isEmpty(receiveAddressList)) {
                jedis.setex(receive_addr_key, 60 * 60 * 1, gson.toJson(receiveAddressList));
            }
            return receiveAddressList;
        }
    }
}