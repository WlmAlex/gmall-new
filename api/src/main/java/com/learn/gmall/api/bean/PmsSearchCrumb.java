package com.learn.gmall.api.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmsSearchCrumb {

    private String valueId;
    private String valueName;
    private String urlParam;
}