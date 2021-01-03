package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.PmsSearchParam;
import com.learn.gmall.api.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> getProductList(PmsSearchParam pmsSearchParam);
}
