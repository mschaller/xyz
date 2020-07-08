package com.orders;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;

import java.time.LocalDateTime;

public class OrderItemActor extends AbstractPersistentActor {
    private final Integer orderId;
    private final LoggingAdapter log;
    private OrderItemActorState state = new OrderItemActorState();
    public OrderItemActor(Integer orderId) {
        this.orderId = orderId;
        log = Logging.getLogger(getContext().system(), this);
        log.info("OrderItemActor created");
    }

    static Props props(Integer orderId) {
        return Props.create(OrderItemActor.class, () -> new OrderItemActor(orderId));
    }

    @Override
    public String persistenceId() {
        return "orderitem-" + orderId.toString();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(OrderItemActorEvent.class, e -> state.update(e))
                .match(SnapshotOffer.class, ss -> state = (OrderItemActorState) ss.snapshot())
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderItemActorMessages.InitOrderItem.class, initOrderItem -> {
                    OrderItemActorEvent evt = new OrderItemActorEvent(OrderItemActorEvent.EventType.TIMECREATED, LocalDateTime.now().toString());
                    persist(evt, (OrderItemActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });
                    evt = new OrderItemActorEvent(OrderItemActorEvent.EventType.STATECHANGE, OrderItemActorState.OrderState.NEW);
                    persist(evt, (OrderItemActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });
                })
                .match(OrderItemActorMessages.OrderPayment.class, changeOrderState -> {
                    if(state.getOrderState().getValue() >= OrderItemActorState.OrderState.PAYED.getValue()) {
                        getSender().tell(new OrderActorMessages.ActionResult("already payed"), getSelf());
                        return;
                    }
                    OrderItemActorEvent evt = new OrderItemActorEvent(OrderItemActorEvent.EventType.PAYMENTINFO, changeOrderState.getPaymentInfo().getPaymentId());
                    persist(evt, (OrderItemActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });
                    evt = new OrderItemActorEvent(OrderItemActorEvent.EventType.STATECHANGE, OrderItemActorState.OrderState.PAYED);
                    persist(evt, (OrderItemActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });

                    getSender().tell(new OrderActorMessages.ActionResult("payment done"), getSelf());
                })
                .match(OrderItemActorMessages.OrderInFulfillment.class, inFulfillment -> {
                    if(state.getOrderState().getValue() < OrderItemActorState.OrderState.PAYED.getValue()) {
                        getSender().tell(new OrderActorMessages.ActionResult("order is not payed yet"), getSelf());
                        return;
                    }
                    OrderItemActorEvent evt = new OrderItemActorEvent(OrderItemActorEvent.EventType.STATECHANGE, OrderItemActorState.OrderState.INFULFILLMENT);
                    persist(evt, (OrderItemActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });
                    getContext().getSystem().actorSelection("/user/fulfillmentActor").tell(new FulfillmentActorMessages.AddOrder(orderId), getSelf());
                    getSender().tell(new OrderActorMessages.ActionResult("order is in fulfillment"), getSelf());
                })
                .match(OrderItemActorMessages.CloseOrder.class, inFulfillment -> {
                    OrderItemActorEvent evt = new OrderItemActorEvent(OrderItemActorEvent.EventType.STATECHANGE, OrderItemActorState.OrderState.CLOSED);
                    persist(evt, (OrderItemActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });
                    evt = new OrderItemActorEvent(OrderItemActorEvent.EventType.FULFILLMENTRESULT, inFulfillment.getFulfillmentResult());
                    persist(evt, (OrderItemActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });
                    getSender().tell(new OrderActorMessages.ActionResult("order " + orderId.toString() + " is closed"), getSelf());
                })
                .match(OrderItemActorMessages.OrderStatus.class, orderStatus -> getSender().tell(state.getOrderState(), getSelf()))
                .match(OrderItemActorMessages.GetOrderDetails.class, orderDetails -> getSender().tell(new OrderActorMessages.ActionResult(state.toString()), getSelf()))
                .matchAny(o -> getSender().tell(new OrderActorMessages.ActionResult("unknown message x"), getSelf()))
                .build();
    }
}
