package com.learn.gmall.api.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmsSkuInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "product_id")
    private Integer spuId;
    private Integer catalog3Id;
    private String skuName;
    private BigDecimal price;
    private Double weight;
    private String skuDesc;
    private String skuDefaultImg;

    @Transient
    private List<PmsSkuAttrValue> skuAttrValueList;

    @Transient
    private List<PmsSkuSaleAttrValue> skuSaleAttrValueList;

    @Transient
    private List<PmsSkuImage> skuImageList;
}
