package com.learn.gmall.manage.service.impl;

import com.learn.gmall.api.api.SpuService;
import com.learn.gmall.api.bean.*;
import com.learn.gmall.manage.mapper.PmsProductImageMapper;
import com.learn.gmall.manage.mapper.PmsProductInfoMapper;
import com.learn.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.learn.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@DubboService
public class SpuServiceImpl implements SpuService {

    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;

    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;

    @Override
    public List<PmsProductInfo> getProductInfoByCatalog3Id(Integer catalog3Id) {
        return pmsProductInfoMapper.select(PmsProductInfo.builder().catalog3Id(catalog3Id).build());
    }

    @Override
    public List<PmsProductSaleAttr> getProductSaleAttrList(Integer spuId) {
        return pmsProductSaleAttrMapper.selectProductSaleAttrList(spuId);

    }

    @Override
    public List<PmsProductImage> getProductImageList(Integer spuId) {
        return pmsProductImageMapper.select(PmsProductImage.builder().productId(spuId).build());
    }

    @Override
    @Transactional
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        pmsProductInfoMapper.insertSelective(pmsProductInfo);

        Integer productId = pmsProductInfo.getId();
        List<PmsProductImage> productImageList = pmsProductInfo.getSpuImageList();
        Optional.ofNullable(productImageList).ifPresent(pmsProductImages -> pmsProductImages.stream()
                .forEach(pmsProductImage -> {
                    pmsProductImage.setProductId(productId);
                    pmsProductImageMapper.insertSelective(pmsProductImage);
                }));
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        Optional.ofNullable(spuSaleAttrList).ifPresent(spuSaleAttrs -> spuSaleAttrs.stream().forEach(spuSaleAttr -> {
            spuSaleAttr.setProductId(productId);
            pmsProductSaleAttrMapper.insertSelective(spuSaleAttr);
            List<PmsProductSaleAttrValue> productSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Optional.ofNullable(productSaleAttrValueList).ifPresent(pmsProductSaleAttrValues -> pmsProductSaleAttrValues
                    .stream().forEach(pmsProductSaleAttrValue -> {
                        pmsProductSaleAttrValue.setProductId(productId);
                        pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
                    }));
        }));
    }

    @Override
    public List<PmsProductSaleAttr> getProductSaleAttrListBySku(PmsSkuInfo skuInfo) {
        return pmsProductSaleAttrMapper.selectProductSaleAttrListBySku(skuInfo);
    }
}