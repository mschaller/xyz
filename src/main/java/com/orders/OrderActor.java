package com.orders;


import akka.actor.*;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;

import java.io.Serializable;
import java.util.*;

public class OrderActor extends AbstractPersistentActor {


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
            int m = 0;
            for (int i = 0; i < orders.size(); i++) {
                if(orders.get(i) > m) {
                    m = orders.get(i);
                }
            }
            return m + 1;
        }
    }

    private OrderActorState state = new OrderActorState();
    private int snapShotInterval = 1000;

    static Props props() {
        return Props.create(OrderActor.class);
    }

    private void createOrderItem(Integer orderid) {
        ActorRef x = getContext().actorOf(OrderItem.props(), Integer.toString(orderid));
        state.update(orderid);
    }

    @Override
    public String persistenceId() {
        return "sample-id-1";
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Integer.class, evt -> state.update(evt))
                .match(SnapshotOffer.class, ss -> {
                    System.out.println("offered state = " + ss);
                    state = (OrderActorState) ss.snapshot();
                })
                .build();
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(Integer.class, i -> {
                    persist(i, evt -> state.update(evt));
                })
                .match(OrderMessages.CreateOrder.class, createOrder -> {
                    int orderid = state.getNextOrderId();
                    createOrderItem(orderid);
                    saveSnapshot(state.copy());
                    getSender().tell(new OrderMessages.ActionResult("order " + Integer.toString(orderid) +  " created"), getSelf());
                })
                .match(OrderMessages.OrderStatus.class, orderStatus -> {
//                    Timeout timeout = new Timeout(Duration.create(15, TimeUnit.SECONDS));
//                    ActorSelection x = getContext().actorSelection("/user/orderActor/12");
//                    Future<Object> future = Patterns.ask(x, new OrderMessages.OrderStatusX(), timeout);
//                    OrderMessages.ActionResult result = (OrderMessages.ActionResult) Await.result(future, timeout.duration());
                    getContext().actorSelection("/user/orderActor/1").tell(new OrderItem.OrderItemMessages.GetOrderStatus(), getSender());
                })
                .match(OrderMessages.HelloWorld.class, getUsers ->
                        getSender().tell(new OrderMessages.ActionResult("hello world"), getSelf()))
                .matchAny(o -> getSender().tell(new OrderMessages.ActionResult("unknown message"), getSelf()))
                .build();
    }


}