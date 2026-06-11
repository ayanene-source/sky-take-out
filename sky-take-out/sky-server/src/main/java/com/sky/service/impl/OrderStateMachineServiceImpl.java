package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.entity.OrderOperationLog;
import com.sky.entity.Orders;
import com.sky.enumeration.OrderOperationAction;
import com.sky.enumeration.OrderOperatorType;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.OrderOperationLogMapper;
import com.sky.service.OrderStateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 订单状态机服务实现类
 * 负责处理订单的各种状态流转逻辑，包括提交、支付、取消、确认、发货、完成等操作，
 * 并记录相应的操作日志。
 */
@Service
public class OrderStateMachineServiceImpl implements OrderStateMachineService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderOperationLogMapper orderOperationLogMapper;

    /**
     * 记录用户提交订单的操作
     *
     * @param order 订单对象
     */
    @Override
    public void recordSubmit(Orders order) {
        saveOperationLog(order.getId(), null, order.getStatus(), OrderOperatorType.USER, BaseContext.getCurrentId(),
                OrderOperationAction.SUBMIT, "submit order");
    }

    /**
     * 处理订单支付成功逻辑
     * 1. 幂等性检查：如果订单已处于待确认状态且支付状态为已支付，则直接返回
     * 2. 状态流转：从待支付状态流转到待确认状态
     * 3. 更新数据：设置支付状态为已支付，并记录结账时间
     *
     * @param order       订单对象
     * @param checkoutTime 结账时间
     */
    @Override
    @Transactional
    public void paySuccess(Orders order, LocalDateTime checkoutTime) {
        // 幂等性检查：防止重复处理支付成功请求
        if (Orders.TO_BE_CONFIRMED.equals(order.getStatus()) && Orders.PAID.equals(order.getPayStatus())) {
            return;
        }
        // 执行状态流转：仅允许从待支付状态流转到待确认状态
        transition(order, Arrays.asList(Orders.PENDING_PAYMENT), Orders.TO_BE_CONFIRMED,
                OrderOperatorType.PAYMENT, null, OrderOperationAction.PAY_SUCCESS, "payment success",
                new OrderUpdateCustomizer() {
                    @Override
                    public void customize(Orders updateOrder) {
                        // 更新支付状态为已支付
                        updateOrder.setPayStatus(Orders.PAID);
                        // 设置结账时间
                        updateOrder.setCheckoutTime(checkoutTime);
                    }
                });
    }

    /**
     * 用户取消订单
     *
     * @param order       订单对象
     * @param refunded    是否已退款
     * @param cancelReason 取消原因
     * @param cancelTime  取消时间
     */
    @Override
    @Transactional
    public void userCancel(Orders order, boolean refunded, String cancelReason, LocalDateTime cancelTime) {
        transition(order, Arrays.asList(Orders.PENDING_PAYMENT, Orders.TO_BE_CONFIRMED), Orders.CANCELLED,
                OrderOperatorType.USER, BaseContext.getCurrentId(), OrderOperationAction.USER_CANCEL, cancelReason,
                new OrderUpdateCustomizer() {
                    @Override
                    public void customize(Orders updateOrder) {
                        updateOrder.setCancelReason(cancelReason);
                        updateOrder.setCancelTime(cancelTime);
                        if (refunded) {
                            updateOrder.setPayStatus(Orders.REFUND);
                        }
                    }
                });
    }

    /**
     * 管理员确认订单
     *
     * @param order 订单对象
     */
    @Override
    @Transactional
    public void adminConfirm(Orders order) {
        transition(order, Arrays.asList(Orders.TO_BE_CONFIRMED), Orders.CONFIRMED,
                OrderOperatorType.ADMIN, BaseContext.getCurrentId(), OrderOperationAction.ADMIN_CONFIRM,
                "admin confirmed order", null);
    }

    /**
     * 管理员拒绝订单
     *
     * @param order           订单对象
     * @param refunded        是否已退款
     * @param rejectionReason 拒绝原因
     * @param cancelTime      取消时间
     */
    @Override
    @Transactional
    public void adminReject(Orders order, boolean refunded, String rejectionReason, LocalDateTime cancelTime) {
        transition(order, Arrays.asList(Orders.TO_BE_CONFIRMED), Orders.CANCELLED,
                OrderOperatorType.ADMIN, BaseContext.getCurrentId(), OrderOperationAction.ADMIN_REJECT, rejectionReason,
                new OrderUpdateCustomizer() {
                    @Override
                    public void customize(Orders updateOrder) {
                        updateOrder.setRejectionReason(rejectionReason);
                        updateOrder.setCancelTime(cancelTime);
                        if (refunded) {
                            updateOrder.setPayStatus(Orders.REFUND);
                        }
                    }
                });
    }

    /**
     * 管理员取消订单
     *
     * @param order       订单对象
     * @param refunded    是否已退款
     * @param cancelReason 取消原因
     * @param cancelTime  取消时间
     */
    @Override
    @Transactional
    public void adminCancel(Orders order, boolean refunded, String cancelReason, LocalDateTime cancelTime) {
        transition(order,
                Arrays.asList(Orders.PENDING_PAYMENT, Orders.TO_BE_CONFIRMED, Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS),
                Orders.CANCELLED, OrderOperatorType.ADMIN, BaseContext.getCurrentId(),
                OrderOperationAction.ADMIN_CANCEL, cancelReason,
                new OrderUpdateCustomizer() {
                    @Override
                    public void customize(Orders updateOrder) {
                        updateOrder.setCancelReason(cancelReason);
                        updateOrder.setCancelTime(cancelTime);
                        if (refunded) {
                            updateOrder.setPayStatus(Orders.REFUND);
                        }
                    }
                });
    }

    /**
     * 开始配送订单
     *
     * @param order 订单对象
     */
    @Override
    @Transactional
    public void startDelivery(Orders order) {
        transition(order, Arrays.asList(Orders.CONFIRMED), Orders.DELIVERY_IN_PROGRESS,
                OrderOperatorType.ADMIN, BaseContext.getCurrentId(), OrderOperationAction.START_DELIVERY,
                "start delivery", null);
    }

    /**
     * 完成订单配送
     *
     * @param order       订单对象
     * @param deliveryTime 配送完成时间
     */
    @Override
    @Transactional
    public void complete(Orders order, LocalDateTime deliveryTime) {
        transition(order, Arrays.asList(Orders.DELIVERY_IN_PROGRESS), Orders.COMPLETED,
                OrderOperatorType.ADMIN, BaseContext.getCurrentId(), OrderOperationAction.COMPLETE_ORDER,
                "complete order",
                new OrderUpdateCustomizer() {
                    @Override
                    public void customize(Orders updateOrder) {
                        updateOrder.setDeliveryTime(deliveryTime);
                    }
                });
    }

    /**
     * 超时自动取消订单
     *
     * @param order       订单对象
     * @param cancelReason 取消原因
     * @param cancelTime  取消时间
     */
    @Override
    @Transactional
    public void autoCancelTimeout(Orders order, String cancelReason, LocalDateTime cancelTime) {
        transition(order, Arrays.asList(Orders.PENDING_PAYMENT), Orders.CANCELLED,
                OrderOperatorType.SYSTEM, null, OrderOperationAction.AUTO_CANCEL_TIMEOUT, cancelReason,
                new OrderUpdateCustomizer() {
                    @Override
                    public void customize(Orders updateOrder) {
                        updateOrder.setCancelReason(cancelReason);
                        updateOrder.setCancelTime(cancelTime);
                    }
                });
    }

    /**
     * 超时自动完成配送
     *
     * @param order       订单对象
     * @param deliveryTime 配送完成时间
     */
    @Override
    @Transactional
    public void autoCompleteDelivery(Orders order, LocalDateTime deliveryTime) {
        transition(order, Arrays.asList(Orders.DELIVERY_IN_PROGRESS), Orders.COMPLETED,
                OrderOperatorType.SYSTEM, null, OrderOperationAction.AUTO_COMPLETE_DELIVERY,
                "auto complete delivery",
                new OrderUpdateCustomizer() {
                    @Override
                    public void customize(Orders updateOrder) {
                        updateOrder.setDeliveryTime(deliveryTime);
                    }
                });
    }

    /**
     * 执行订单状态流转的核心逻辑
     * 1. 校验订单是否存在
     * 2. 校验当前状态是否允许流转到目标状态
     * 3. 构建更新对象并执行自定义定制（如设置支付状态、取消时间等）
     * 4. 更新订单数据
     * 5. 记录操作日志
     *
     * @param currentOrder   当前订单对象
     * @param allowedStatuses 允许流转的源状态列表
     * @param targetStatus    目标状态
     * @param operatorType    操作人类型
     * @param operatorId      操作人ID
     * @param action          操作动作枚举
     * @param remark          备注信息
     * @param customizer      订单更新定制器，用于设置额外字段
     */
    private void transition(Orders currentOrder,
                            List<Integer> allowedStatuses,
                            Integer targetStatus,
                            OrderOperatorType operatorType,
                            Long operatorId,
                            OrderOperationAction action,
                            String remark,
                            OrderUpdateCustomizer customizer) {
        // 校验订单是否存在
        if (currentOrder == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 校验当前状态是否允许转换
        if (!allowedStatuses.contains(currentOrder.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(currentOrder.getId())
                .status(targetStatus)
                .build();
        if (customizer != null) {
            customizer.customize(updateOrder);
        }
        orderMapper.update(updateOrder);// 更新订单
        // 记录操作日志
        saveOperationLog(currentOrder.getId(), currentOrder.getStatus(), targetStatus, operatorType, operatorId, action, remark);
    }

    /**
     * 保存订单操作日志
     *
     * @param orderId      订单ID
     * @param beforeStatus 变更前状态
     * @param afterStatus  变更后状态
     * @param operatorType 操作人类型
     * @param operatorId   操作人ID
     * @param action       操作动作
     * @param remark       备注信息
     */
    private void saveOperationLog(Long orderId,
                                  Integer beforeStatus,
                                  Integer afterStatus,
                                  OrderOperatorType operatorType,
                                  Long operatorId,
                                  OrderOperationAction action,
                                  String remark) {
        OrderOperationLog operationLog = OrderOperationLog.builder()
                .orderId(orderId)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .operatorType(operatorType.name())
                .operatorId(operatorId)
                .action(action.name())
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();
        orderOperationLogMapper.insert(operationLog);
    }

    /**
     * 订单更新定制器接口
     * 用于在状态流转过程中自定义更新订单的其他字段
     */
    private interface OrderUpdateCustomizer {
        /**
         * 定制订单更新内容
         *
         * @param updateOrder 待更新的订单对象
         */
        void customize(Orders updateOrder);
    }
}
