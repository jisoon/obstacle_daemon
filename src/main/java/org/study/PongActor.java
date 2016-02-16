package org.study;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * @author : 지순
 * @packageName : org.study
 * @since : 2016-02-16
 */
public class PongActor extends UntypedActor{

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef ping;

    public PongActor(ActorRef ping) {
        this.ping = ping;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            String msg = (String)message;
            log.info("Pong received {}", msg);
            ping.tell("pong", getSelf());
            Thread.sleep(1000);
        }
    }
}
