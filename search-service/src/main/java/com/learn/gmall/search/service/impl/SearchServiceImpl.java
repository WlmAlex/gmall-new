package com.learn.gmall.search.service.impl;

import com.learn.gmall.api.api.SearchService;
import com.learn.gmall.api.bean.PmsSearchParam;
import com.learn.gmall.api.bean.PmsSearchSkuInfo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@DubboService
public class SearchServiceImpl implements SearchService {

    @Autowired
    private JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> getProductList(PmsSearchParam pmsSearchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueId = pmsSearchParam.getValueId();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.must(termQueryBuilder);
        }

        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        if (ArrayUtils.isNotEmpty(valueId)) {
            Set<String> valueIdList = new HashSet<>();
            for (String value : valueId) {
                if (StringUtils.isNotBlank(value)) {
                    valueIdList.add(value);
                }
            }

            if (valueIdList != null && valueIdList.size() != 0) {
                TermsQueryBuilder termQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId", valueIdList);
                boolQueryBuilder.must(termQueryBuilder);

            }
        }
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        highlightBuilder.preTags("<span style=color:red;>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        System.err.println(searchSourceBuilder.toString());
        try {
            SearchResult searchResult = jestClient.execute(new Search.Builder(searchSourceBuilder.toString()).addIndex("gmall").addType("PmsSkuInfo").build());
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
            List<PmsSearchSkuInfo> pmsSearchSkuInfoList = hits.stream()
                    .filter(hit -> hit != null)
                    .map(hit -> {
                        PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
                        Map<String, List<String>> highlight = hit.highlight;
                        if (!CollectionUtils.isEmpty(highlight) && highlight.get("skuName") != null) {
                            pmsSearchSkuInfo.setSkuName(highlight.get("skuName").get(0));
                        }
                        return pmsSearchSkuInfo;
                    })
                    .collect(Collectors.toList());
            return pmsSearchSkuInfoList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.singletonList(PmsSearchSkuInfo.builder().build());
    }
}
