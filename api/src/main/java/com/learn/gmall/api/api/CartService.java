package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.OmsCartItem;
import com.learn.gmall.api.bean.OmsOrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    List<OmsCartItem> getCartListByMemberId(String memberId);

    void updateCartList(List<OmsCartItem> omsCartItemList);

    void addToCart(OmsCartItem omsCartItem);

    void updateCartItemStatus(OmsCartItem omsCartItem);

    BigDecimal checkQuantity(Integer productSkuId, String memberId);

    void clearCart(OmsOrderItem omsOrderItem);
}
