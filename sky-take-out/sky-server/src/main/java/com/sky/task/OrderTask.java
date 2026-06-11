package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderStateMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    private static final int PAYMENT_TIMEOUT_MINUTES = 15;
    private static final int DELIVERY_TIMEOUT_HOURS = 24;
    private static final String CANCEL_REASON_TIMEOUT = "支付超时，系统自动取消";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStateMachineService orderStateMachineService;

    /**
     * 每分钟扫描一次超时未支付订单，并通过状态机统一取消。
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutOrder() {
        log.info("开始处理支付超时订单，执行时间：{}", LocalDateTime.now());
        try {
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
            List<Orders> ordersList = orderMapper.getByStatusAndTimeLT(Orders.PENDING_PAYMENT, timeoutTime);

            if (ordersList.isEmpty()) {
                log.info("暂无支付超时订单需要处理");
                return;
            }

            log.info("共查询到{}条支付超时订单，开始通过状态机逐单处理", ordersList.size());
            int updateCount = 0;
            LocalDateTime cancelTime = LocalDateTime.now();
            for (Orders order : ordersList) {
                orderStateMachineService.autoCancelTimeout(order, CANCEL_REASON_TIMEOUT, cancelTime);
                updateCount++;
            }

            log.info("支付超时订单处理完成，成功更新{}条订单", updateCount);
        } catch (Exception e) {
            log.error("处理支付超时订单时发生异常", e);
            throw new RuntimeException("处理支付超时订单失败", e);
        }
    }

    /**
     * 每天凌晨扫描长时间配送中的订单，并通过状态机自动完成。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void processDeliveryOrder() {
        log.info("开始处理待派送超时订单，执行时间：{}", LocalDateTime.now());
        try {
            LocalDateTime timeoutTime = LocalDateTime.now().minusHours(DELIVERY_TIMEOUT_HOURS);
            List<Orders> ordersList = orderMapper.getByStatusAndTimeLT(Orders.DELIVERY_IN_PROGRESS, timeoutTime);

            if (ordersList.isEmpty()) {
                log.info("暂无待派送超时订单需要处理");
                return;
            }

            log.info("共查询到{}条待派送超时订单，开始通过状态机逐单处理", ordersList.size());
            int updateCount = 0;
            LocalDateTime deliveryTime = LocalDateTime.now();
            for (Orders order : ordersList) {
                orderStateMachineService.autoCompleteDelivery(order, deliveryTime);
                updateCount++;
            }

            log.info("待派送超时订单处理完成，成功更新{}条订单", updateCount);
        } catch (Exception e) {
            log.error("处理待派送超时订单时发生异常", e);
            throw new RuntimeException("处理待派送超时订单失败", e);
        }
    }
}
