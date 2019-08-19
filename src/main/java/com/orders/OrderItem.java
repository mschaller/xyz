package com.orders;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.io.Serializable;

public class OrderItem extends AbstractActor {
    public interface OrderItemMessages {
        class GetOrderStatus implements Serializable {
        }
    }

    static Props props() {
        return Props.create(OrderItem.class);
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(OrderItemMessages.GetOrderStatus.class, orderStatus -> {
                    getSender().tell(new OrderMessages.ActionResult("hello world"), getSelf());
                })
                .matchAny(o -> getSender().tell(new OrderMessages.ActionResult("unknown message x"), getSelf()))
                .build();
    }
}
