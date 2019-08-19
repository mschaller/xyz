package com.orders;

import java.io.Serializable;

public interface OrderMessages {

    class HelloWorld implements Serializable {}

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

    class OrderStatus implements Serializable { }

    class OrderStatusX implements Serializable { }
}