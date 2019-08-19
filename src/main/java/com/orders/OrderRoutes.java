package com.orders;


import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;

public class OrderRoutes extends AllDirectives {
    final private ActorRef orderActor;
    final private LoggingAdapter log;


    public OrderRoutes(ActorSystem system, ActorRef orderActor) {
        this.orderActor = orderActor;
        log = Logging.getLogger(system, this);
    }

    Duration timeout = Duration.ofSeconds(5l); // usually we'd obtain the timeout from the system's configuration

    public Route routes() {
        return concat(
                pathPrefix("order", () ->
                        route(
                            get(() -> {
                                CompletionStage<OrderMessages.ActionResult> result = Patterns
                                        .ask(orderActor, new OrderMessages.OrderStatus(), timeout)
                                        .thenApply(OrderMessages.ActionResult.class::cast);

                                return onSuccess(() -> result,
                                        performed -> {
                                            return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                        });
                            }),
                            post(() -> {
                                CompletionStage<OrderMessages.ActionResult> result = Patterns
                                        .ask(orderActor, new OrderMessages.CreateOrder(), timeout)
                                        .thenApply(OrderMessages.ActionResult.class::cast);
                                return onSuccess(() -> result,
                                        performed -> {
                                            return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                        });
                            })
                        )
                ),

                pathPrefix("helloworld", () ->
                        get(() -> {
                            CompletionStage<OrderMessages.ActionResult> result = Patterns
                                    .ask(orderActor, new OrderMessages.HelloWorld(), timeout)
                                    .thenApply(OrderMessages.ActionResult.class::cast);
                            return onSuccess(() -> result,
                                    performed -> {
                                        return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                    });
                        })
                )
        );
    }
}
