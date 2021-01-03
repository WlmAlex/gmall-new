package com.learn.gmall.api.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsOrderMessageSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String memberId;
    private String messageContent;
    private String status;
    private Boolean isAlive;
    private Integer retryCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime nextRetryTime;
}
