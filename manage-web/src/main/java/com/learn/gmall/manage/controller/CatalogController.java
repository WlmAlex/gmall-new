package com.learn.gmall.manage.controller;

import com.learn.gmall.api.api.CatalogService;
import com.learn.gmall.api.bean.PmsBaseCatalog1;
import com.learn.gmall.api.bean.PmsBaseCatalog2;
import com.learn.gmall.api.bean.PmsBaseCatalog3;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class CatalogController {

    @DubboReference
    private CatalogService catalogService;

    @PostMapping("/getCatalog1")
    public List<PmsBaseCatalog1> getCatalog1List() {
        return catalogService.getPmsBaseCatalog1List();
    }

    @PostMapping("/getCatalog2")
    public List<PmsBaseCatalog2> getCatalog2ListByCatalog1Id(Integer catalog1Id) {
        return catalogService.getPmsBaseCatalog2ListByCatalog1Id(catalog1Id);
    }

    @PostMapping("/getCatalog3")
    public List<PmsBaseCatalog3> getCatalog3ListByCatalog2Id(Integer catalog2Id) {
        return catalogService.getPmsBaseCatalog3ListByCatalog2Id(catalog2Id);
    }


}
