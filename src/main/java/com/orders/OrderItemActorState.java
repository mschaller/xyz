package com.orders;

import java.io.Serializable;
import java.time.LocalDateTime;


public class OrderItemActorState implements Serializable {
    public enum OrderState {
        NEW(0),
        PAYED(1),
        INFULFILLMENT(2),
        CLOSED(3);

        private final int value;
        private OrderState(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private LocalDateTime timeCreated;
    private OrderState orderState;
    private String paymentId;
    private String fulfillmentResult;

    public OrderItemActorState() {
        paymentId = "";
        fulfillmentResult = "";
    }

    public void update(OrderItemActorEvent e) {
        switch(e.getEventType()) {
            case TIMECREATED:
                timeCreated =  LocalDateTime.parse(e.getEventValue().toString());
                break;
            case STATECHANGE:
                OrderState newValue = OrderState.valueOf(e.getEventValue().toString());
                if(orderState == null || newValue.getValue() > orderState.getValue()) {
                    orderState = newValue;
                }
                break;
            case PAYMENTINFO:
                paymentId = e.getEventValue().toString();
                break;
            case FULFILLMENTRESULT:
                fulfillmentResult = e.getEventValue().toString();
                break;
            default:
                break;
        }
    }

    public OrderState getOrderState() {
        return orderState;
    }

    @Override
    public String toString() {
        return "tc: " + timeCreated.toString() + " state: " + orderState.toString() + " paymentid: " + paymentId + " fulfillmentResult: " + fulfillmentResult;
    }
}
