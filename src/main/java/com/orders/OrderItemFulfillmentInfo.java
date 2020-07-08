package com.orders;

import java.io.Serializable;

public class OrderItemFulfillmentInfo implements Serializable {
    private Integer orderId;

    public OrderItemFulfillmentInfo() {
    }

    public OrderItemFulfillmentInfo(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getOrderId() {
        return orderId;
    }
}
