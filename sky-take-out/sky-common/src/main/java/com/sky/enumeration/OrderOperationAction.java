package com.sky.enumeration;

/**
 * 订单操作动作
 */
public enum OrderOperationAction {
    SUBMIT,
    PAY_SUCCESS,
    USER_CANCEL,
    ADMIN_CONFIRM,
    ADMIN_REJECT,
    ADMIN_CANCEL,
    START_DELIVERY,
    COMPLETE_ORDER,
    AUTO_CANCEL_TIMEOUT,
    AUTO_COMPLETE_DELIVERY
}
