package com.learn.gmall.order.mapper;

import com.learn.gmall.api.bean.OmsOrderMessageSendLog;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OmsOrderMsgMapper extends Mapper<OmsOrderMessageSendLog> {
    List<OmsOrderMessageSendLog> selectStatus2AndIsAliveMsg();
}
