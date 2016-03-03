package com.neonex;

import akka.actor.*;
import com.neonex.message.StartMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] ar) {
        logger.info(">>>>>>>>>>>>>>> OBSTACLE_DAEMON START!!!");
        ActorSystem system = ActorSystem.create("obstacle-daemon");
        ActorRef daemon = system.actorOf(Props.create(DeviceActor.class), "deviceStatus");
        system.scheduler().schedule(Duration.Zero(), Duration.create(60, TimeUnit.SECONDS), daemon, new StartMsg(), system.dispatcher(), null);
    }

}
