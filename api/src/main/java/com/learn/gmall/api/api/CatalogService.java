package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.PmsBaseCatalog1;
import com.learn.gmall.api.bean.PmsBaseCatalog2;
import com.learn.gmall.api.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {

    List<PmsBaseCatalog1> getPmsBaseCatalog1List();

    List<PmsBaseCatalog2> getPmsBaseCatalog2ListByCatalog1Id(Integer catalog1Id);

    List<PmsBaseCatalog3> getPmsBaseCatalog3ListByCatalog2Id(Integer catalog2Id);
}
