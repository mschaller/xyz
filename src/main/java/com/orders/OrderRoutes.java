package com.orders;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import static akka.http.javadsl.server.PathMatchers.integerSegment;
import static akka.http.javadsl.server.PathMatchers.segment;
import akka.pattern.Patterns;

public class OrderRoutes extends AllDirectives {
    private final ActorRef orderActor;
    private final ActorRef fulfillmentActor;


    public OrderRoutes(ActorRef orderActor, ActorRef fulfillmentActor) {
        this.orderActor = orderActor;
        this.fulfillmentActor = fulfillmentActor;
    }

    Duration timeout = Duration.ofSeconds(5l); // usually we'd obtain the timeout from the system's configuration

    public Route routes() {
        return concat(
            pathPrefix("orders", () ->
                route(
                    get(() -> {
                        CompletionStage<OrderActorMessages.ActionResult> result = Patterns
                            .ask(orderActor, new OrderActorMessages.OrderActorStatus(), timeout)
                            .thenApply(OrderActorMessages.ActionResult.class::cast);

                        return onSuccess(() -> result, performed -> complete(StatusCodes.CREATED, performed, Jackson.marshaller()));
                    })
                )
            ),
            pathPrefix("createOrder", () ->
                route(
                    post(() -> {
                        CompletionStage<OrderActorMessages.ActionResult> result = Patterns
                            .ask(orderActor, new OrderActorMessages.CreateOrder(), timeout)
                            .thenApply(OrderActorMessages.ActionResult.class::cast);
                        return onSuccess(() -> result, performed ->  complete(StatusCodes.CREATED, performed, Jackson.marshaller()));
                    })
                )
            ),
            path(segment("order").slash(integerSegment()), orderid ->
                get(() -> {
                    CompletionStage<OrderActorMessages.ActionResult> result = Patterns
                        .ask(orderActor, new OrderActorMessages.OrderDetails(orderid), timeout)
                        .thenApply(OrderActorMessages.ActionResult.class::cast);

                    return onSuccess(() -> result, performed -> complete(StatusCodes.CREATED, performed, Jackson.marshaller()));

                })
            ),
            pathPrefix("payOrder", () ->
                post(() ->
                    entity(Jackson.unmarshaller(OrderItemPaymentInfo.class), paymentInfo -> {
                        CompletionStage<OrderActorMessages.ActionResult> result = Patterns
                            .ask(orderActor, new OrderActorMessages.OrderPayment(paymentInfo), timeout)
                            .thenApply(OrderActorMessages.ActionResult.class::cast);

                        return onSuccess(() -> result, performed -> complete(StatusCodes.CREATED, performed, Jackson.marshaller()));
                    })
                )
            ),
            pathPrefix("fulfillOrder", () ->
                post(() ->
                    entity(Jackson.unmarshaller(OrderItemFulfillmentInfo.class), fulfillmentInfo -> {
                        CompletionStage<OrderActorMessages.ActionResult> result = Patterns
                            .ask(orderActor, new OrderActorMessages.OrderInFulfillment(fulfillmentInfo), timeout)
                            .thenApply(OrderActorMessages.ActionResult.class::cast);

                        return onSuccess(() -> result, performed -> complete(StatusCodes.CREATED, performed, Jackson.marshaller()));
                    })
                )
            ),
            pathPrefix("closeOrder", () ->
                post(() ->
                    entity(Jackson.unmarshaller(OrderItemClosedInfo.class), closedInfo -> {
                        CompletionStage<OrderActorMessages.ActionResult> result = Patterns
                                .ask(fulfillmentActor, new FulfillmentActorMessages.ProcessOrder(), timeout)
                                .thenApply(OrderActorMessages.ActionResult.class::cast);

                        return onSuccess(() -> result, performed -> complete(StatusCodes.CREATED, performed, Jackson.marshaller()));
                    })
                )
            )
        );
    }
}
