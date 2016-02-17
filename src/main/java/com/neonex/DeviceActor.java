package com.neonex;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.neonex.dto.DeviceStatus;
import com.neonex.message.StartMsg;
import com.neonex.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@SuppressWarnings("JpaQlInspection")
public class DeviceActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private SessionFactory sessionFactory;


    public DeviceActor() {
        sessionFactory = HibernateUtils.getSessionFactory();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            log.info(">>>>>>>> 장애데몬 시작");
        } else {
            unhandled(message);
        }
    }

    /**
     * 현재 연결된 장비들의 상태 정보 조회
     *
     * @return
     */
    public List<DeviceStatus> fetchDevice(Session session) {
        session = getSession(session);
        List<DeviceStatus> deviceStatus = session.createCriteria(DeviceStatus.class)
                .add(Restrictions.eq("connectYn", "Y"))
                .list();
        session.close();
        return deviceStatus;
    }

    public int detectDisconnect(Session session, List<DeviceStatus> devices) {
        session = getSession(session);
        int disconnectCount = 0;
        try {
            for (DeviceStatus device : devices) {
                System.out.println(device.toString());
                if (device.getLastCommTime() != null) {
                    long dbLastCommTime = Long.parseLong(device.getLastCommTime());
                    long thresholdLastCommTime = Long.parseLong("20160217164225");
                    System.out.println(dbLastCommTime);
                    System.out.println(thresholdLastCommTime);
                    if (dbLastCommTime < thresholdLastCommTime) {
                        DeviceStatus disconnectDevice = new DeviceStatus();
                        disconnectDevice.setConnectYn("N");
                        disconnectDevice.setEqId(device.getEqId());
                        session.update(disconnectDevice);
                        disconnectCount++;
                    }
                }
            }
        } catch (NumberFormatException ne) {
            System.out.println("NumberFormatException ");
            ne.printStackTrace();
            //skip
        } catch (Exception e) {
            System.out.println("Exception ");
            e.printStackTrace();
        }

        return disconnectCount;
    }

    private Session getSession(Session session) {
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        return session;
    }
}
