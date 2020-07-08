package com.orders;

import java.io.Serializable;
import java.util.ArrayList;

public class FulfillmentActorState implements Serializable {
    private final ArrayList<Integer> orders;

    public FulfillmentActorState() {
        this(new ArrayList<>());
    }

    public FulfillmentActorState(ArrayList<Integer> orders) {
        this.orders = orders;
    }

    public OrderActorState copy() {
        return new OrderActorState(new ArrayList<>(orders));
    }

    public void update(FulfillmentActorEvent event) {
        if(event.getDone()) {
            orders.remove(event.getOrderId());
        } else {
            orders.add(event.getOrderId());
        }
    }

    public boolean contains(Integer orderId) {
        return orders.contains(orderId);
    }

    public Integer peekOrderId() {
        if(orders.isEmpty())
            return -1;

        return orders.get(0);
    }
}
