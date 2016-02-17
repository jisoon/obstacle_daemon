package com.neonex;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.neonex.message.StartMsg;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
public class DeviceStatus extends UntypedActor{
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            log.info(">>>>>>>> 장애데몬 시작");
        }else{
            unhandled(message);
        }
    }

    public boolean fetchDeviceStatus(){

        return true;
    }
}
