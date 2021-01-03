package com.learn.gmall.manage.mapper;

import com.learn.gmall.api.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectAttrInfoList(Integer catalog3Id);

    List<PmsBaseAttrInfo> selectAttrInfoListByPrimaryKey(@Param("valueIdList") List<Integer> valueIdList);
}
