package com.neonex;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.neonex.message.StartMsg;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
public class Main {
    public static void main(String[] ar) {
        ActorSystem actorSystem = ActorSystem.create("obstacle_daemon");
        ActorRef daemon = actorSystem.actorOf(Props.create(DeviceActor.class), "deviceStatus");
        daemon.tell(new StartMsg(), ActorRef.noSender());
    }

}
