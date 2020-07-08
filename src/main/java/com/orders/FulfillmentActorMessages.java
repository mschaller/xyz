package com.orders;


import java.io.Serializable;

public interface FulfillmentActorMessages {
    class AddOrder implements Serializable {
        private final Integer orderId;

        public AddOrder(Integer orderId) {
            this.orderId = orderId;
        }

        public Integer getOrderId() {
            return orderId;
        }
    }

    class ProcessOrder implements Serializable  { }
}
