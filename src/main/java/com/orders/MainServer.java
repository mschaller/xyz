package com.orders;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.util.concurrent.CompletionStage;

public class MainServer extends AllDirectives {
    private final OrderRoutes orderRoutes;

    public MainServer(ActorRef orderActor, ActorRef fulfillmentActor) {
        orderRoutes = new OrderRoutes(orderActor, fulfillmentActor);
    }

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("sys");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        ActorRef orderActor = system.actorOf(OrderActor.props(), "orderActor");
        ActorRef fulfillmentActor = system.actorOf(FulfillmentActor.props(), "fulfillmentActor");
        MainServer app = new MainServer(orderActor, fulfillmentActor);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

        System.in.read(); // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    public Route createRoute() {
        return orderRoutes.routes();
    }

}