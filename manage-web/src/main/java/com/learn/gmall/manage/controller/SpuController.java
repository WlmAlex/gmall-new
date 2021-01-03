package com.learn.gmall.manage.controller;

import com.learn.gmall.api.api.SpuService;
import com.learn.gmall.api.bean.PmsProductImage;
import com.learn.gmall.api.bean.PmsProductInfo;
import com.learn.gmall.api.bean.PmsProductSaleAttr;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class SpuController {

    @DubboReference
    private SpuService spuService;

    @GetMapping("/spuList")
    public List<PmsProductInfo> getProductList(Integer catalog3Id) {
        return spuService.getProductInfoByCatalog3Id(catalog3Id);
    }

    @GetMapping("/spuSaleAttrList")
    public List<PmsProductSaleAttr> getProductSaleAttrList(Integer spuId) {
        return spuService.getProductSaleAttrList(spuId);
    }

    @GetMapping("/spuImageList")
    public List<PmsProductImage> getProductImageList(Integer spuId) {
        return spuService.getProductImageList(spuId);
    }

    @PostMapping("/saveSpuInfo")
    public void saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuService.saveSpuInfo(pmsProductInfo);
    }
}