package com.learn.gmall.cart.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.learn.gmall.api.api.CartService;
import com.learn.gmall.api.api.SkuService;
import com.learn.gmall.api.bean.OmsCartItem;
import com.learn.gmall.api.bean.PmsSkuInfo;
import com.learn.gmall.web.annotation.LoginRequired;
import com.learn.gmall.web.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class CartController {

    @DubboReference
    private SkuService skuService;

    private static final Gson gson = new Gson();

    @DubboReference
    private CartService cartService;

    @GetMapping("/cartList")
    @LoginRequired
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        //String memberId = (String) request.getAttribute("memberId");
        String memberId = "1";
        //用户已登录
        if (StringUtils.isNotBlank(memberId)) {
            //用户已经登录, 使用Db作为购物车

            //1. 先看购物车是否有对应商品
            List<OmsCartItem> omsCartItemList = cartService.getCartListByMemberId(memberId);

            if (!CollectionUtils.isEmpty(omsCartItemList)) {
                modelMap.put("cartList", omsCartItemList);
                modelMap.put("totalBalance", getTotalBalance(omsCartItemList));
            }
        } else {
            //用户未登录, 使用cookie作为购物车
            String cookieValue = CookieUtil.getCookieValue(request, "gmall-cart-cookie", true);

            if (StringUtils.isNotBlank(cookieValue)) {
                //1. 先看购物车是否有对应商品
                List<OmsCartItem> omsCartItemList = gson.fromJson(cookieValue, new TypeToken<List<OmsCartItem>>() {
                }.getType());
                modelMap.put("cartList", omsCartItemList);
                modelMap.put("totalBalance", getTotalBalance(omsCartItemList));
            }
        }
        modelMap.put("userId", memberId);
        return "cartList";
    }

    @PostMapping("/addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(Integer skuId, BigDecimal quantity, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        System.err.println(skuId);
        PmsSkuInfo pmsSkuInfo = skuService.getPmsSkuInfoById(skuId);
        //String memberId = (String) request.getAttribute("memberId");
        String memberId = "1";
        OmsCartItem omsCartItem = OmsCartItem.builder()
                .memberId(memberId)
                .productSkuId(skuId)
                .productPic(pmsSkuInfo.getSkuDefaultImg())
                .productName(pmsSkuInfo.getSkuName())
                .productBrand("ddd")
                .createDate(new Date())
                .modifyDate(new Date())
                .deleteStatus(0)
                .memberNickname("wlm123")
                .price(pmsSkuInfo.getPrice())
                .quantity(quantity)
                .productId(pmsSkuInfo.getSpuId())
                .build();

        //用户已登录
        if (StringUtils.isNotBlank(memberId)) {
            //用户已经登录, 使用Db作为购物车

            //1. 先看购物车是否有对应商品
            List<OmsCartItem> omsCartItemList = cartService.getCartListByMemberId(memberId);

            if (!CollectionUtils.isEmpty(omsCartItemList)) {
                Optional<OmsCartItem> cartItemOptional = omsCartItemList.stream()
                        .filter(omsCartItemTmp -> omsCartItemTmp.getProductSkuId() == omsCartItem.getProductSkuId())
                        .findFirst();
                //a. 有对应商品, 则增加数量
                if (cartItemOptional.isPresent()) {
                    OmsCartItem omsCartItemTmp = cartItemOptional.get();
                    omsCartItemTmp.setQuantity(omsCartItemTmp.getQuantity().add(omsCartItem.getQuantity()));

                } else {
                    //b. 没有对应商品, 则添加商品
                    omsCartItemList.add(omsCartItem);

                    cartService.addToCart(omsCartItem);
                }

            } else {
                omsCartItemList = new ArrayList<>();
                omsCartItemList.add(omsCartItem);

                cartService.addToCart(omsCartItem);
            }
            //更新db
            cartService.updateCartList(omsCartItemList);
        } else {
            //用户未登录, 使用cookie作为购物车
            String cookieValue = CookieUtil.getCookieValue(request, "gmall-cart-cookie", true);

            List<OmsCartItem> omsCartItemList = new ArrayList<>();
            if (StringUtils.isNotBlank(cookieValue)) {
                //1. 先看购物车是否有对应商品
                omsCartItemList = gson.fromJson(cookieValue, new TypeToken<List<OmsCartItem>>() {
                }.getType());

                Optional<OmsCartItem> cartItemOptional = omsCartItemList.stream()
                        .filter(omsCartItemTmp -> omsCartItemTmp.getProductSkuId() == omsCartItem.getProductSkuId())
                        .findFirst();
                //a. 有对应商品, 则增加数量
                if (cartItemOptional.isPresent()) {
                    OmsCartItem omsCartItemTmp = cartItemOptional.get();
                    omsCartItemTmp.setQuantity(omsCartItemTmp.getQuantity().add(omsCartItem.getQuantity()));
                } else {
                    //b. 没有对应商品, 则添加商品
                    omsCartItemList.add(omsCartItem);
                }
            } else {
                //购物车为空, 没有商品, 直接添加
                omsCartItemList.add(omsCartItem);
            }
            CookieUtil.setCookie(request, response, "gmall-cart-cookie", gson.toJson(omsCartItemList), 30 * 60, true);
        }
        modelMap.put("skuInfo", pmsSkuInfo);
        modelMap.put("skuNum", quantity);
        return "success";
    }

    @PostMapping("/checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(OmsCartItem omsCartItem, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        System.out.println("skuId: " + omsCartItem.getProductSkuId() + "..." + "checkStatus: " + omsCartItem.getIsChecked());
        //String memberId = (String) request.getAttribute("memberId");
        String memberId = "1";

        //已登录
        if (StringUtils.isNotBlank(memberId)) {
            omsCartItem.setMemberId(memberId);
            List<OmsCartItem> omsCartItemList = cartService.getCartListByMemberId(memberId);
            if (!CollectionUtils.isEmpty(omsCartItemList)) {
                omsCartItemList.stream()
                        .filter(omsCartItemTmp -> omsCartItemTmp.getProductSkuId() == omsCartItem.getProductSkuId())
                        .forEach(omsCartItemTmp -> omsCartItemTmp.setIsChecked(omsCartItem.getIsChecked()));

                cartService.updateCartList(omsCartItemList);
                modelMap.put("totalBalance", getTotalBalance(omsCartItemList));
                modelMap.put("cartList", omsCartItemList);
            }
        } else {
            //未登录
            String cookieValue = CookieUtil.getCookieValue(request, "gmall-cart-cookie", true);
            if (StringUtils.isNotBlank(cookieValue)) {
                List<OmsCartItem> omsCartItemList = gson.fromJson(cookieValue, new TypeToken<List<OmsCartItem>>() {
                }.getType());

                omsCartItemList.stream()
                        .filter(omsCartItemTmp -> omsCartItemTmp.getProductSkuId() == omsCartItem.getProductSkuId())
                        .forEach(omsCartItemTmp -> omsCartItemTmp.setIsChecked(omsCartItem.getIsChecked()));
                modelMap.put("cartList", omsCartItemList);
                modelMap.put("totalBalance", getTotalBalance(omsCartItemList));
                CookieUtil.setCookie(request, response, "gmall-cart-cookie", gson.toJson(omsCartItemList), 30 * 60, true);
            }
        }
        return "cartlistInner";
    }

    private BigDecimal getTotalBalance(List<OmsCartItem> omsCartItemList) {
        BigDecimal result = omsCartItemList.stream()
                .filter(omsCartItem -> "1".equals(omsCartItem.getIsChecked()))
                .map(omsCartItem -> omsCartItem.getQuantity().multiply(omsCartItem.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return result;
    }
}