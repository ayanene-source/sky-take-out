CREATE TABLE IF NOT EXISTS `order_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `action` varchar(64) NOT NULL COMMENT '操作动作',
  `before_status` int DEFAULT NULL COMMENT '变更前状态',
  `after_status` int DEFAULT NULL COMMENT '变更后状态',
  `operator_type` varchar(32) NOT NULL COMMENT '操作人类型',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人id',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_operation_log_order_id` (`order_id`),
  KEY `idx_order_operation_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志表';
