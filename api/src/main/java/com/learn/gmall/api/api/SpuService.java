package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.PmsProductImage;
import com.learn.gmall.api.bean.PmsProductInfo;
import com.learn.gmall.api.bean.PmsProductSaleAttr;
import com.learn.gmall.api.bean.PmsSkuInfo;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> getProductInfoByCatalog3Id(Integer catalog3Id);

    List<PmsProductSaleAttr> getProductSaleAttrList(Integer spuId);

    List<PmsProductImage> getProductImageList(Integer spuId);

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> getProductSaleAttrListBySku(PmsSkuInfo skuInfo);
}
