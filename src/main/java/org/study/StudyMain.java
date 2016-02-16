package org.study;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * @author : 지순
 * @packageName : org.study
 * @since : 2016-02-16
 */
public class StudyMain {

    public static void main(String[] ar) {
        ActorSystem actorSystem = ActorSystem.create("TestActorSystem");
        ActorRef ping = actorSystem.actorOf(Props.create(PingActor.class), "pingActor");
        ping.tell("start", ActorRef.noSender());
    }
}
