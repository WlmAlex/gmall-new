package com.learn.gmall.manage.controller;

import com.learn.gmall.api.api.SkuService;
import com.learn.gmall.api.bean.PmsSkuInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class SkuController {

    @DubboReference
    private SkuService skuService;

    @PostMapping("/saveSkuInfo")
    public void saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo) {
        skuService.saveSkuInfo(pmsSkuInfo);
    }
}
