package com.sky.service;

import com.sky.entity.Orders;

import java.time.LocalDateTime;

public interface OrderStateMachineService {

    void recordSubmit(Orders order);

    void paySuccess(Orders order, LocalDateTime checkoutTime);

    void userCancel(Orders order, boolean refunded, String cancelReason, LocalDateTime cancelTime);

    void adminConfirm(Orders order);

    void adminReject(Orders order, boolean refunded, String rejectionReason, LocalDateTime cancelTime);

    void adminCancel(Orders order, boolean refunded, String cancelReason, LocalDateTime cancelTime);

    void startDelivery(Orders order);

    void complete(Orders order, LocalDateTime deliveryTime);

    void autoCancelTimeout(Orders order, String cancelReason, LocalDateTime cancelTime);

    void autoCompleteDelivery(Orders order, LocalDateTime deliveryTime);
}
