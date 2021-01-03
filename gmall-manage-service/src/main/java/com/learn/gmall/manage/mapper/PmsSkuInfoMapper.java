package com.learn.gmall.manage.mapper;

import com.learn.gmall.api.bean.PmsSkuInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {
    PmsSkuInfo selectPmsSkuInfoByPrimaryKey(@Param("skuId") Integer skuId);

    List<PmsSkuInfo> selectPmsSkuInfoListByProductId(@Param("spuId") Integer spuId);

    PmsSkuInfo selectPmsSkuInfoBySkuId(@Param("skuId")Integer skuId);
}
