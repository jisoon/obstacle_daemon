package com.neonex;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.neonex.message.StartMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] ar) {

        logger.info(">>>>>>>>>>>>>>> OBSTACLE_DAEMON START!!!");
        ActorSystem actorSystem = ActorSystem.create("obstacle_daemon");
        ActorRef daemon = actorSystem.actorOf(Props.create(DeviceActor.class), "deviceStatus");
        daemon.tell(new StartMsg(), ActorRef.noSender());
    }

}
