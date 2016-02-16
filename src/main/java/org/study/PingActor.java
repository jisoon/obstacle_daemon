package org.study;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * @author : 지순
 * @packageName : org.study
 * @since : 2016-02-16
 */
public class PingActor extends UntypedActor{
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef pong;

    @Override
    public void preStart() {
        this.pong = context().actorOf(Props.create(PongActor.class, getSelf()));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            String msg = (String)message;
            log.info("Ping received {}", msg);
            pong.tell("ping", getSelf());
        }
    }
}
