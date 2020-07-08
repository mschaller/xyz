package com.orders;


//#test-top

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import akka.http.javadsl.model.*;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import org.junit.Before;
import org.junit.Test;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;


public class MainServerTest extends JUnitRouteTest {
    private TestRoute appRoute;


    @Before
    public void initClass() {
        ActorSystem system = ActorSystem.create("helloAkkaHttpServer");
        ActorRef orderActor = system.actorOf(OrderActor.props(), "orderActor");
        ActorRef fulfillmentActor = system.actorOf(FulfillmentActor.props(), "fulfillmentActor");
        MainServer server = new MainServer(orderActor, fulfillmentActor);
        appRoute = testRoute(server.createRoute());
    }

    @Test
    public void testCreateOrder() {
        appRoute.run(HttpRequest.POST("/createOrderX"))
                .assertStatusCode(StatusCodes.CREATED)
                .assertMediaType("application/json");
    }
}

