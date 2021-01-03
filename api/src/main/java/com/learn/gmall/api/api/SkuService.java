package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getPmsSkuInfoById(Integer skuId);

    List<PmsSkuInfo> getPmsSkuInfoListByProductId(Integer spuId);

    BigDecimal checkPrice(Integer productSkuId);
}
