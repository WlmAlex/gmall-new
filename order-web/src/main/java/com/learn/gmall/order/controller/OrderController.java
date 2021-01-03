package com.learn.gmall.order.controller;

import com.learn.gmall.api.api.CartService;
import com.learn.gmall.api.api.OrderService;
import com.learn.gmall.api.api.SkuService;
import com.learn.gmall.api.api.UserService;
import com.learn.gmall.api.bean.OmsCartItem;
import com.learn.gmall.api.bean.OmsOrder;
import com.learn.gmall.api.bean.OmsOrderItem;
import com.learn.gmall.api.bean.UmsMemberReceiveAddress;
import com.learn.gmall.web.annotation.LoginRequired;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @DubboReference
    private UserService userService;

    @DubboReference
    private OrderService orderService;

    @DubboReference
    private CartService cartService;

    @DubboReference
    private SkuService skuService;

    @PostMapping("/submitOrder")
    @LoginRequired
    public String submitOrder(OmsOrder omsOrder, String tradeCode, HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        modelMap.put("nickName", request.getAttribute("nickname"));
        boolean checkResult = orderService.checkTradeCode(memberId, tradeCode);
        if (checkResult) {
            System.err.println(omsOrder);

            //check price and quantity
            BigDecimal totalAmount = Optional.ofNullable(omsOrder.getOmsOrderItems()).map(omsOrderItems -> omsOrderItems.stream()
                    .map(omsOrderItem -> skuService.checkPrice(omsOrderItem.getProductSkuId()).multiply(cartService.checkQuantity(omsOrderItem.getProductSkuId(), memberId)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)).orElseGet(() -> new BigDecimal("0"));
            if (totalAmount.intValue() == 0) {
                modelMap.put("errMsg", "订单价格或数量错误, 请重新提交");
                return "tradeFail";
            }
            omsOrder.setTotalAmount(totalAmount);
            omsOrder.setMemberId(memberId);
            omsOrder.setOrderSn("gmall" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + omsOrder.getTotalAmount());
            omsOrder.setCreateTime(new Date());
            orderService.saveOrder(omsOrder);
            modelMap.put("omsOrder", omsOrder);
            return "list";
        }
        modelMap.put("errMsg", "订单重复提交");

        return "tradeFail";
    }

    @GetMapping("/toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = String.valueOf(request.getAttribute("nickname"));
        List<UmsMemberReceiveAddress> receiveAddressList = userService.getReceiveAddressByMemberId(memberId);
        modelMap.put("userAddressList", receiveAddressList);
        List<OmsCartItem> cartItemList = cartService.getCartListByMemberId(memberId);
        List<OmsOrderItem> orderItemList = Optional.ofNullable(cartItemList).map(omsCartItems -> omsCartItems.stream()
                .filter(omsCartItem -> "1".equals(omsCartItem.getIsChecked()))
                .map(omsCartItem -> OmsOrderItem.builder()
                        .productId(omsCartItem.getProductId())
                        .productSkuId(omsCartItem.getProductSkuId())
                        .productName(omsCartItem.getProductName())
                        .productPic(omsCartItem.getProductPic())
                        .productQuantity(omsCartItem.getQuantity())
                        .productPrice(omsCartItem.getPrice())
                        .productPic(omsCartItem.getProductPic())
                        .realAmount(omsCartItem.getQuantity().multiply(omsCartItem.getPrice()))
                        .build())
                .collect(Collectors.toList()))
                .orElseGet(() -> new ArrayList<>());

        modelMap.put("orderDetailList", orderItemList);

        modelMap.put("totalAmount", getTotalAmount(orderItemList));

        modelMap.put("tradeCode", orderService.generateTradeCode(memberId));

        modelMap.put("nickName", nickname);
        return "trade";
    }

    private BigDecimal getTotalAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal totalAmount = Optional.of(orderItemList).map(omsOrderItems -> omsOrderItems.stream()
                .map(omsOrderItem -> omsOrderItem.getRealAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        ).orElseGet(() -> new BigDecimal("0"));
        return totalAmount;
    }
}