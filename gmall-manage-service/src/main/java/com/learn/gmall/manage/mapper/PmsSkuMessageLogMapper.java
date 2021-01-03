package com.learn.gmall.manage.mapper;

import com.learn.gmall.api.bean.PmsSkuMessageLog;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuMessageLogMapper extends Mapper<PmsSkuMessageLog> {
    List<PmsSkuMessageLog> selectSentFailMessageList();
}
