package com.learn.gmall.api.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmsBaseCatalog3 implements Serializable {

    @Id
    private Integer id;
    private String name;
    private Integer catalog2Id;

    @Transient
    private List<PmsBaseAttrInfo> baseAttrInfoList;
}
