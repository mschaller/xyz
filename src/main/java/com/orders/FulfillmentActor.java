package com.orders;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class FulfillmentActor extends AbstractPersistentActor {
    private final SecureRandom rnd = new SecureRandom();
    private final LoggingAdapter log;
    private FulfillmentActorState state = new FulfillmentActorState();

    public FulfillmentActor() {
        log = Logging.getLogger(getContext().system(), this);
        log.info("FulfillmentActor created");
    }

    static Props props() {
        return Props.create(FulfillmentActor.class);
    }

    @Override
    public String persistenceId() {
        return "fulfillment-1";
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(FulfillmentActorEvent.class, e -> {
                    log.debug("Fulfillment evt: " + e.toString());
                    state.update(e);
                })
                .match(SnapshotOffer.class, ss -> {
                    log.debug("offered state = " + ss);
                    state = (FulfillmentActorState) ss.snapshot();
                })
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FulfillmentActorMessages.AddOrder.class, addOrder -> {
                    if(state.contains(addOrder.getOrderId())) {
                        return;
                    }

                    FulfillmentActorEvent evt = new FulfillmentActorEvent(addOrder.getOrderId(), false);
                    persist(evt, (FulfillmentActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });

                    if(lastSequenceNr() % 50 == 0 && lastSequenceNr() != 0) {
                        log.info("saving snapshot");
                        saveSnapshot(state.copy());
                    }
                })
                .match(FulfillmentActorMessages.ProcessOrder.class, processOrder -> {
                    Integer orderId = state.peekOrderId();
                    if(orderId < 0) {
                        getSender().tell(new OrderActorMessages.ActionResult("nothing to complete"), getSelf());
                        return;
                    }

                    Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
                    ActorSelection x = getContext().actorSelection("/user/orderActor");
                    Future<Object> future = Patterns.ask(x, new OrderActorMessages.OrderStatus(orderId), timeout);
                    OrderItemActorState.OrderState result = (OrderItemActorState.OrderState) Await.result(future, timeout.duration());

                    if(result.getValue() < OrderItemActorState.OrderState.PAYED.getValue()) {
                        getSender().tell(new OrderActorMessages.ActionResult("order is not payed"), getSelf());
                        return;
                    }

                    String fulfillmentResult = rnd.nextInt() % 2 == 0 ? "SUCCESS" : "FAILURE";
                    x.tell(new OrderActorMessages.OrderClosed(orderId, fulfillmentResult), getSender());

                    FulfillmentActorEvent evt = new FulfillmentActorEvent(orderId, true);
                    persist(evt, (FulfillmentActorEvent e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                    });

                    if(lastSequenceNr() % 50 == 0 && lastSequenceNr() != 0) {
                        log.info("saving snapshot of orderactor");
                        saveSnapshot(state.copy());
                    }
                })
                .matchAny(o -> getSender().tell(new OrderActorMessages.ActionResult("unknown message y"), getSelf()))
                .build();
    }
}
