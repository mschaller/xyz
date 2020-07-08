package com.orders;

import java.io.Serializable;

public class OrderItemPaymentInfo implements Serializable {
    private Integer orderId;
    private String paymentId;

    public OrderItemPaymentInfo() {
        this(0, "");
    }

    public OrderItemPaymentInfo(Integer orderId, String paymentId) {
        this.orderId = orderId;
        this.paymentId = paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    @Override
    public String toString() {
        return "orderId: " + orderId.toString() + ", paymentId: " + paymentId;
    }
}
