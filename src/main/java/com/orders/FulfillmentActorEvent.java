package com.orders;

import java.io.Serializable;

public class FulfillmentActorEvent implements Serializable {
    private final Integer orderId;
    private final boolean isDone;

    public FulfillmentActorEvent(Integer orderId, Boolean isDone) {
        this.orderId = orderId;
        this.isDone = isDone;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public boolean getDone() {
        return isDone;
    }

    @Override
    public String toString() {
        return ((isDone) ? "remove orderid: " : "add orderid: ") + orderId.toString();
    }

}
