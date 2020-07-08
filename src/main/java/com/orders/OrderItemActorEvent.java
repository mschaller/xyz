package com.orders;

import java.io.Serializable;

public class OrderItemActorEvent implements Serializable {
    public enum EventType {
        TIMECREATED,
        STATECHANGE,
        PAYMENTINFO,
        FULFILLMENTRESULT,
    }

    private final EventType eventType;
    private final Object eventValue;

    public OrderItemActorEvent(EventType eventType, Object eventValue) {
        this.eventType = eventType;
        this.eventValue = eventValue;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Object getEventValue() {
        return eventValue;
    }

    @Override
    public String toString() {
        return eventType.toString() + ":" + eventValue.toString();
    }
}
