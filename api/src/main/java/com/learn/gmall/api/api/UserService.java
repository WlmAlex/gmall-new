package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.UmsMember;
import com.learn.gmall.api.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String id);

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
