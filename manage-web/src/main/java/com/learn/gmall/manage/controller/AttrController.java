package com.learn.gmall.manage.controller;

import com.learn.gmall.api.api.AttrService;
import com.learn.gmall.api.bean.PmsBaseAttrInfo;
import com.learn.gmall.api.bean.PmsBaseAttrValue;
import com.learn.gmall.api.bean.PmsBaseSaleAttr;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class AttrController {

    @DubboReference
    private AttrService attrService;

    @GetMapping("/attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(Integer catalog3Id) {
        return attrService.getAttrInfoListByCatalog3Id(catalog3Id);
    }

    @PostMapping("/getAttrValueList")
    public List<PmsBaseAttrValue> getAttrValueListByAttrId(Integer attrId) {
        return attrService.getAttrValueListByAttrId(attrId);
    }

    @PostMapping("/saveAttrInfo")
    public void saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {
        attrService.saveAttrInfo(pmsBaseAttrInfo);
    }

    @PostMapping("/baseSaleAttrList")
    public List<PmsBaseSaleAttr> getBaseSaleAttrList() {
        return attrService.getBaseSaleAttrList();
    }
}