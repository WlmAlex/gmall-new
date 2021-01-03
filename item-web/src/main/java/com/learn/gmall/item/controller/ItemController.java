package com.learn.gmall.item.controller;

import com.google.gson.Gson;
import com.learn.gmall.api.api.SkuService;
import com.learn.gmall.api.api.SpuService;
import com.learn.gmall.api.bean.PmsProductSaleAttr;
import com.learn.gmall.api.bean.PmsSkuInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ItemController {

    @DubboReference
    private SkuService skuService;

    @DubboReference
    private SpuService spuService;

    @GetMapping("/{skuId}.html")
    public String itemDetail(@PathVariable Integer skuId, ModelMap modelMap) {

        PmsSkuInfo skuInfo = skuService.getPmsSkuInfoById(skuId);
        modelMap.put("skuInfo", skuInfo);

        if (skuInfo.getId() != null) {
            List<PmsProductSaleAttr> spuSaleAttrListCheckBySku = spuService.getProductSaleAttrListBySku(skuInfo);
            modelMap.put("spuSaleAttrListCheckBySku", spuSaleAttrListCheckBySku);

            //查询当前sku所属的spu下的, 所有的sku集合
            List<PmsSkuInfo> pmsSkuInfoList = skuService.getPmsSkuInfoListByProductId(skuInfo.getSpuId());

            Map<String, Integer> paramsMap = new HashMap<>();
            pmsSkuInfoList.stream().forEach(pmsSkuInfo -> {
                Integer value = pmsSkuInfo.getId();
                String key = pmsSkuInfo.getSkuSaleAttrValueList().stream()
                        .map(pmsSkuSaleAttrValue -> String.format("%s|", pmsSkuSaleAttrValue.getSaleAttrValueId()))
                        .collect(Collectors.joining());
                paramsMap.put(key, value);
            });

            modelMap.put("skuSaleAttrHashJsonStr", new Gson().toJson(paramsMap));
        }
        return "item";
    }
}