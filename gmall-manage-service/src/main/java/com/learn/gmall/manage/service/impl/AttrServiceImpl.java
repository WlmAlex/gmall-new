package com.learn.gmall.manage.service.impl;

import com.learn.gmall.api.api.AttrService;
import com.learn.gmall.api.bean.PmsBaseAttrInfo;
import com.learn.gmall.api.bean.PmsBaseAttrValue;
import com.learn.gmall.api.bean.PmsBaseSaleAttr;
import com.learn.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.learn.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.learn.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@DubboService
public class AttrServiceImpl implements AttrService {

    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoListByCatalog3Id(Integer catalog3Id) {
        return pmsBaseAttrInfoMapper.selectAttrInfoList(catalog3Id);
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueListByAttrId(Integer attrId) {
        return pmsBaseAttrValueMapper.select(PmsBaseAttrValue.builder().attrId(attrId).build());
    }

    @Override
    @Transactional
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        if (pmsBaseAttrInfo.getId() == null) {
            //insert pms_base_attr_info and pms_base_attr_value into db
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

        }
        //update attr values in db
        List<PmsBaseAttrValue> baseAttrValueList = pmsBaseAttrInfo.getAttrValueList();
        pmsBaseAttrValueMapper.delete(PmsBaseAttrValue.builder().attrId(pmsBaseAttrInfo.getId()).build());
        Optional.ofNullable(baseAttrValueList).ifPresent(baseAttrValues -> baseAttrValues.stream()
                .forEach(baseAttrValue -> {
                    baseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                    pmsBaseAttrValueMapper.insertSelective(baseAttrValue);
                }));

    }

    @Override
    public List<PmsBaseSaleAttr> getBaseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoListByPrimayKey(List<Integer> valueIdList) {
        return pmsBaseAttrInfoMapper.selectAttrInfoListByPrimaryKey(valueIdList);
    }
}
