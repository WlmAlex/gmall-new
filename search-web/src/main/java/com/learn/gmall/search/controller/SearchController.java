package com.learn.gmall.search.controller;

import com.learn.gmall.api.api.AttrService;
import com.learn.gmall.api.api.SearchService;
import com.learn.gmall.api.bean.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @DubboReference
    private SearchService searchService;

    @DubboReference
    private AttrService attrService;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.getProductList(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfoList);

        List<Integer> valueId = pmsSearchSkuInfoList.stream()
                .filter(pmsSearchSkuInfo -> !CollectionUtils.isEmpty(pmsSearchSkuInfo.getSkuAttrValueList()))
                .map(pmsSearchSkuInfo -> {
                    List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
                    List<Integer> valueIdList = skuAttrValueList.stream().map(PmsSkuAttrValue::getValueId).distinct().collect(Collectors.toList());
                    return valueIdList;
                })
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(valueId)) {
            List<PmsBaseAttrInfo> attrList = attrService.getAttrInfoListByPrimayKey(valueId);
            modelMap.put("attrList", attrList);

            String[] valueIdArray = pmsSearchParam.getValueId();
            List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(valueIdArray)) {
                for (String value : valueIdArray) {
                    PmsSearchCrumb pmsSearchCrumb = PmsSearchCrumb.builder()
                            .valueId(value)
                            .valueName("")
                            .urlParam(getUrlParamFromCrumb(pmsSearchParam, value))
                            .build();

                    ListIterator<PmsBaseAttrInfo> listIterator = attrList.listIterator();
                    while (listIterator.hasNext()) {
                        PmsBaseAttrInfo pmsBaseAttrInfo = listIterator.next();
                        for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
                            String id = String.valueOf(pmsBaseAttrValue.getId());
                            if (id.equals(value)) {
                                pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                                listIterator.remove();
                            }
                        }
                    }
                    pmsSearchCrumbList.add(pmsSearchCrumb);
                }


            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbList);
        }
        //modelMap.put("keyword", pmsSearchParam.getKeyword());

        String urlParam = generateUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        return "list";
    }

    private String getUrlParamFromCrumb(PmsSearchParam pmsSearchParam, String value) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueIdArray = pmsSearchParam.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isBlank(urlParam)) {
                urlParam = urlParam + "keyword=" + keyword;
            } else {
                urlParam = urlParam + "&keyword=" + keyword;
            }
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isBlank(urlParam)) {
                urlParam = urlParam + "catalog3Id=" + catalog3Id;
            } else {
                urlParam = urlParam + "&catalog3Id=" + catalog3Id;
            }
        }

        if (ArrayUtils.isNotEmpty(valueIdArray)) {
            for (String valueId : valueIdArray) {
                if (!value.equals(valueId)) {
                    urlParam = urlParam + "valueId=" + valueId;
                }
            }
        }
        return urlParam;
    }

    private String generateUrlParam(PmsSearchParam pmsSearchParam) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueIdArray = pmsSearchParam.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isBlank(urlParam)) {
                urlParam = urlParam + "keyword=" + keyword;
            } else {
                urlParam = urlParam + "&keyword=" + keyword;
            }
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isBlank(urlParam)) {
                urlParam = urlParam + "catalog3Id=" + catalog3Id;
            } else {
                urlParam = urlParam + "&catalog3Id=" + catalog3Id;
            }
        }

        if (ArrayUtils.isNotEmpty(valueIdArray)) {
            for (String valueId : valueIdArray) {
                if (StringUtils.isBlank(urlParam)) {
                    urlParam = urlParam + "valueId=" + valueId;
                } else {
                    urlParam = urlParam + "&valueId=" + valueId;
                }
            }
        }
        return urlParam;
    }
}
