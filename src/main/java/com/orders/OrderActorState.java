package com.orders;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderActorState implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ArrayList<Integer> orders;

    public OrderActorState() {
        this(new ArrayList<>());
    }

    public OrderActorState(ArrayList<Integer> orders) {
        this.orders = orders;
    }

    public OrderActorState copy() {
        return new OrderActorState(new ArrayList<>(orders));
    }

    public void update(Integer order) {
        orders.add(order);
    }

    public Integer getNextOrderId() {
        if(orders.isEmpty()) {
            return 1;
        }
        return orders.get(orders.size() - 1) + 1;
    }
}