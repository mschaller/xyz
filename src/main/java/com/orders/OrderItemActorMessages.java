package com.orders;

import java.io.Serializable;

public interface OrderItemActorMessages {
    class InitOrderItem implements Serializable { }

    class GetOrderDetails implements Serializable { }

    class OrderStatus implements Serializable { }

    class OrderInFulfillment implements  Serializable { }

    class CloseOrder implements  Serializable {
        private final String fulfillmentResult;

        public CloseOrder(String fulfillmentResult) {
            this.fulfillmentResult = fulfillmentResult;
        }

        public String getFulfillmentResult() {
            return fulfillmentResult;
        }
    }

    class OrderPayment implements Serializable {
        private final OrderItemPaymentInfo paymentInfo;

        public OrderPayment(OrderItemPaymentInfo paymentInfo) {
            this.paymentInfo = paymentInfo;
        }

        public OrderItemPaymentInfo getPaymentInfo() {
            return paymentInfo;
        }
    }
}