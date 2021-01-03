package com.learn.gmall.manage.mapper;

import com.learn.gmall.api.bean.PmsProductSaleAttr;
import com.learn.gmall.api.bean.PmsSkuInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> selectProductSaleAttrList(Integer spuId);

    List<PmsProductSaleAttr> selectProductSaleAttrListBySku(@Param("skuInfo") PmsSkuInfo skuInfo);
}
