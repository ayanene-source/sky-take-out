package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单操作日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private String action;

    private Integer beforeStatus;

    private Integer afterStatus;

    private String operatorType;

    private Long operatorId;

    private String remark;

    private LocalDateTime createTime;
}
