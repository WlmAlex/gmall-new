package com.learn.gmall.manage.task;

import com.learn.gmall.api.bean.PmsSkuInfo;
import com.learn.gmall.api.bean.PmsSkuMessageLog;
import com.learn.gmall.manage.component.PmsSkuInfoSender;
import com.learn.gmall.manage.mapper.PmsSkuInfoMapper;
import com.learn.gmall.manage.mapper.PmsSkuMessageLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class RetryPmsSkuInfoScheduler {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuMessageLogMapper pmsSkuMessageLogMapper;

    @Autowired
    private PmsSkuInfoSender pmsSkuInfoSender;

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void resendPmsSkuInfo() {
        System.out.println("===============================开启定时任务====================: " + LocalDateTime.now().toString());
        List<PmsSkuMessageLog> messageLogList = pmsSkuMessageLogMapper.selectSentFailMessageList();

        Optional.ofNullable(messageLogList).ifPresent(messageLogs -> messageLogs.stream().forEach(messageLog -> {
            if (messageLog.getRetryCount() >= 3) {
                messageLog.setIsDead("true");
                messageLog.setUpdateTime(LocalDateTime.now());
                pmsSkuMessageLogMapper.updateByPrimaryKeySelective(messageLog);
            } else {
                PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectPmsSkuInfoBySkuId(messageLog.getSkuId());
                pmsSkuInfoSender.sendPmsSkuInfo(pmsSkuInfo);
            }
        }));
    }
}
