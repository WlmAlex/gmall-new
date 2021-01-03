package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.PmsBaseAttrInfo;
import com.learn.gmall.api.bean.PmsBaseAttrValue;
import com.learn.gmall.api.bean.PmsBaseSaleAttr;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> getAttrInfoListByCatalog3Id(Integer catalog3Id);

    List<PmsBaseAttrValue> getAttrValueListByAttrId(Integer attrId);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseSaleAttr> getBaseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrInfoListByPrimayKey(List<Integer> valueIdList);
}
