package com.orders;

import java.io.Serializable;

public interface OrderActorMessages {

    class HelloWorld implements Serializable {}

    class OrderActorStatus implements Serializable { }

    class ActionResult implements Serializable {
        private final String resultText;

        public ActionResult(String resultText) {
            this.resultText = resultText;
        }

        public String getResultText() {
            return resultText;
        }
    }

    class CreateOrder implements Serializable { }

    class OrderPayment implements Serializable {
        private OrderItemPaymentInfo paymentInfo;

        public OrderPayment(OrderItemPaymentInfo paymentInfo) {
            this.paymentInfo = paymentInfo;
        }

        public OrderItemPaymentInfo getPaymentInfo() {
            return paymentInfo;
        }
    }

    class OrderDetails implements Serializable {
        private final Integer orderId;

        public OrderDetails(Integer orderId) {
            this.orderId = orderId;
        }

        public Integer getOrderId() {
            return orderId;
        }
    }

    class OrderStatus implements Serializable {
        private final Integer orderId;

        public OrderStatus(Integer orderId) {
            this.orderId = orderId;
        }

        public Integer getOrderId() {
            return orderId;
        }
    }

    class OrderInFulfillment implements Serializable {
        OrderItemFulfillmentInfo fulfillmentInfo;

        public OrderInFulfillment(OrderItemFulfillmentInfo fulfillmentInfo) {
            this.fulfillmentInfo = fulfillmentInfo;
        }

        public OrderItemFulfillmentInfo getFulfillmentInfo() {
            return fulfillmentInfo;
        }
    }

    class OrderClosed implements Serializable {
        private String fulfillmentResult;
        private Integer orderId;

        public OrderClosed(Integer orderId, String fulfillmentResult) {
            this.fulfillmentResult = fulfillmentResult;
            this.orderId = orderId;
        }

        public String getFulfillmentResult() {
            return fulfillmentResult;
        }

        public Integer getOrderId() {
            return orderId;
        }
    }

}