package com.learn.gmall.manage.service.impl;

import com.learn.gmall.api.api.CatalogService;
import com.learn.gmall.api.bean.PmsBaseCatalog1;
import com.learn.gmall.api.bean.PmsBaseCatalog2;
import com.learn.gmall.api.bean.PmsBaseCatalog3;
import com.learn.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.learn.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.learn.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@DubboService
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    private PmsBaseCatalog1Mapper catalog1Mapper;

    @Autowired
    private PmsBaseCatalog2Mapper catalog2Mapper;

    @Autowired
    private PmsBaseCatalog3Mapper catalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getPmsBaseCatalog1List() {
        return catalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getPmsBaseCatalog2ListByCatalog1Id(Integer catalog1Id) {
        Example example = new Example(PmsBaseCatalog2.class);
        example.createCriteria().andEqualTo("catalog1Id", catalog1Id);
        return catalog2Mapper.selectByExample(example);
    }

    @Override
    public List<PmsBaseCatalog3> getPmsBaseCatalog3ListByCatalog2Id(Integer catalog2Id) {
        Example example = new Example(PmsBaseCatalog3.class);
        example.createCriteria().andEqualTo("catalog2Id", catalog2Id);
        return catalog3Mapper.selectByExample(example);
    }
}
