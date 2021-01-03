package com.learn.gmall.order.mapper;

import com.learn.gmall.api.bean.OmsOrder;
import com.learn.gmall.api.bean.OmsOrderItem;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface OmsOrderItemMapper extends Mapper<OmsOrderItem> {
    void saveOrderItemList(@Param("omsOrder") OmsOrder omsOrder);
}
