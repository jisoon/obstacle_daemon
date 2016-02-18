package com.neonex;

import akka.actor.UntypedActor;
import com.neonex.dto.DeviceStatus;
import com.neonex.message.StartMsg;
import com.neonex.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@SuppressWarnings("JpaQlInspection")
public class DeviceActor extends UntypedActor {

    private final static Logger logger = LoggerFactory.getLogger(DeviceActor.class);

    private SessionFactory sessionFactory;


    public DeviceActor() {
        sessionFactory = HibernateUtils.getSessionFactory();
    }
    public DeviceActor(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            logger.info("=== message received!!! ===");
            List<DeviceStatus> devices = fetchDevice();
            int disconnecCount = detectDisconnect(devices);
            logger.info("disconnect count {}", disconnecCount);
        } else {
            unhandled(message);
        }
    }

    /**
     * 현재 연결된 장비들의 상태 정보 조회
     *
     * @return
     */
    public List<DeviceStatus> fetchDevice() {
        logger.info("=== fetchDevice ===");
        Session session = sessionFactory.openSession();
        List<DeviceStatus> deviceStatus = session.createCriteria(DeviceStatus.class)
                .add(Restrictions.eq("connectYn", "Y"))
                .list();
        session.close();
        return deviceStatus;
    }

    public int detectDisconnect(List<DeviceStatus> devices) {
        logger.info("=== detectDisconnect === ");
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        int disconnectCount = 0;
        try {
            for (DeviceStatus device : devices) {
                if (device.getLastCommTime() != null) {
                    long dbLastCommTime = Long.parseLong(device.getLastCommTime());
                    long thresholdLastCommTime = Long.parseLong("20160217164225");
                    if (dbLastCommTime < thresholdLastCommTime) {
                        logger.info("disconnec target device  > {} lastCommTime > {}", device.getEqId(), dbLastCommTime);
                        DeviceStatus disconnectDevice = new DeviceStatus();
                        disconnectDevice.setConnectYn("N");
                        disconnectDevice.setEqId(device.getEqId());
                        disconnectDevice.setLastCommTime(device.getLastCommTime());
                        session.update(disconnectDevice);
                        disconnectCount++;
                    }
                }
            }
        } catch (NumberFormatException ne) {
            logger.info("NumberFormatException ");
            ne.printStackTrace();
            //skip
        } catch (Exception e) {
            logger.info("Exception ");
            e.printStackTrace();
        }
        session.getTransaction().commit();
        session.close();
        return disconnectCount;
    }

    public DeviceStatus findDevice(String eqId) {
        Session session = sessionFactory.openSession();
        DeviceStatus device = session.get(DeviceStatus.class, eqId);
        session.close();
        return device;
    }
}
