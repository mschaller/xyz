package com.orders;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SaveSnapshotFailure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;

import java.util.ArrayList;


public class OrderActor extends AbstractPersistentActor {
    private static final String ITEMPATH = "/user/orderActor/";
    private final ArrayList<Integer> initializedOrders = new ArrayList<>();
    private final LoggingAdapter log;
    private OrderActorState state = new OrderActorState();
    public OrderActor() {
        log = Logging.getLogger(getContext().system(), this);
        log.info("OrderActor created");
    }

    static Props props() {
        return Props.create(OrderActor.class);
    }

    private boolean checkOrderItemActorExists(Integer orderid) {
        return initializedOrders.contains(orderid);
    }

    private void restoreOrderItem(Integer orderId) {
        getContext().actorOf(OrderItemActor.props(orderId), Integer.toString(orderId));
        initializedOrders.add(orderId);
    }

    private void createOrderItem(Integer orderId) {
        if(checkOrderItemActorExists(orderId)) {
            return;
        }

        ActorRef x = getContext().actorOf(OrderItemActor.props(orderId), Integer.toString(orderId));
        x.tell(new OrderItemActorMessages.InitOrderItem(), getSelf());

        initializedOrders.add(orderId);
    }

    @Override
    public String persistenceId() {
        return "orderactor-1";
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Integer.class, e -> {
                    state.update(e);
                    log.info("evt: " + e.toString());
                })
                .match(SnapshotOffer.class, ss -> {
                    log.info("offered state = " + ss);
                    state = (OrderActorState) ss.snapshot();
                })
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SaveSnapshotSuccess.class, sf -> log.info("snapshot saved"))
                .match(SaveSnapshotFailure.class, sf -> log.info("snapshot failed"))
                .match(OrderActorMessages.CreateOrder.class, createOrder -> {
                    int orderid = state.getNextOrderId();
                    createOrderItem(orderid);
                    persist(orderid, (Integer e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });

                    if(lastSequenceNr() % 50 == 0 && lastSequenceNr() != 0) {
                        log.info("saving snapshot of orderactor");
                        saveSnapshot(state.copy());
                    }
                    getSender().tell(new OrderActorMessages.ActionResult("order " + orderid + " created"), getSelf());
                })
                .match(OrderActorMessages.OrderPayment.class, orderPayment -> {
                    if(!checkOrderItemActorExists(orderPayment.getPaymentInfo().getOrderId())) {
                        restoreOrderItem(orderPayment.getPaymentInfo().getOrderId());
                    }
                    getContext().actorSelection(ITEMPATH + orderPayment.getPaymentInfo().getOrderId()).tell(new OrderItemActorMessages.OrderPayment(orderPayment.getPaymentInfo()), getSender());
                })
                .match(OrderActorMessages.OrderInFulfillment.class, orderInFulfillment -> {
                    if(!checkOrderItemActorExists(orderInFulfillment.getFulfillmentInfo().getOrderId())) {
                        restoreOrderItem(orderInFulfillment.getFulfillmentInfo().getOrderId());
                    }
                    getContext().actorSelection(ITEMPATH + orderInFulfillment.getFulfillmentInfo().getOrderId().toString()).tell(new OrderItemActorMessages.OrderInFulfillment(), getSender());
                })
                .match(OrderActorMessages.OrderDetails.class, orderDetails -> {
                    if(!checkOrderItemActorExists(orderDetails.getOrderId())) {
                        restoreOrderItem(orderDetails.getOrderId());
                    }
                    getContext().actorSelection(ITEMPATH + orderDetails.getOrderId().toString()).tell(new OrderItemActorMessages.GetOrderDetails(), getSender());
                })
                .match(OrderActorMessages.OrderStatus.class, orderStatus -> {
                    if(!checkOrderItemActorExists(orderStatus.getOrderId())) {
                        restoreOrderItem(orderStatus.getOrderId());
                    }
                    getContext().actorSelection(ITEMPATH + orderStatus.getOrderId().toString()).tell(new OrderItemActorMessages.OrderStatus(), getSender());
                })
                .match(OrderActorMessages.OrderClosed.class, orderClosed -> {
                    if(!checkOrderItemActorExists(orderClosed.getOrderId())) {
                        restoreOrderItem(orderClosed.getOrderId());
                    }
                    getContext().actorSelection(ITEMPATH + orderClosed.getOrderId().toString()).tell(new OrderItemActorMessages.CloseOrder(orderClosed.getFulfillmentResult()), getSender());
                })
                .match(OrderActorMessages.OrderActorStatus.class, getUsers -> getSender().tell(new OrderActorMessages.ActionResult(Integer.toString(state.getNextOrderId() - 1)), getSelf()))
                .matchAny(o -> getSender().tell(new OrderActorMessages.ActionResult("unknown message"), getSelf()))
                .build();
    }


}