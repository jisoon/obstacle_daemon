package com.neonex;

import akka.actor.UntypedActor;
import com.neonex.dto.DeviceStatus;
import com.neonex.dto.EventLog;
import com.neonex.message.StartMsg;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@SuppressWarnings("JpaQlInspection")
public class DeviceActor extends UntypedActor {

    private final static Logger logger = LoggerFactory.getLogger(DeviceActor.class);

    private SessionFactory sessionFactory;


    // argment constructor 를 사용할때 반드시 기본 constructor 도 필요하다.
    public DeviceActor() {
        logger.info("default construnctor");
    }

    public DeviceActor(SessionFactory sessionFactory) {
        logger.info("SessionFactory construnctor");
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

    int detectDisconnect(List<DeviceStatus> devices) {
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
                        logger.info("disconnec target device : {} lastCommTime : {}", device.getEqId(), dbLastCommTime);

                        updateStatusDisconnect(session, device);

                        insertDisconnectEvent(session, device.getEqId());

                        disconnectCount++;
                    }
                }
            }
        } catch (NumberFormatException ne) {
            session.getTransaction().rollback();
        } catch (Exception e) {
            logger.info("Exception ");
            e.printStackTrace();
        }
        session.getTransaction().commit();
        session.close();
        return disconnectCount;
    }

    DeviceStatus findDevice(String eqId) {
        Session session = sessionFactory.openSession();
        DeviceStatus device = session.get(DeviceStatus.class, eqId);
        session.close();
        return device;
    }

    boolean
    insertDisconnectEvent(Session session, String eqId) {
        logger.info("=== insertDisconnectEvent ===");
        EventLog eventLog = new EventLog();
        eventLog.setEqId(eqId);
        eventLog.setEventCont("미연결");
        eventLog.setEventLv(1);
        eventLog.setProcessVn("N");
        eventLog.setOccurDate(currentTime());
        eventLog.setEventSeq(getEventSeq(session));
        logger.info(eventLog.toString());


        try {
            session.save(eventLog);
            return true;
        } catch (Exception e) {
            logger.error("insert disconnect device error", e);
            return false;
        }

    }

    private Long getEventSeq(Session session) {
        Query query = session.createSQLQuery("select SQNT_EQ_EVENT_LOG_SEQ.nextval from dual");
        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    void updateStatusDisconnect(Session session, DeviceStatus device) {
        logger.info("=== updateStatusDisconnect ===");
        DeviceStatus disconnectDevice = new DeviceStatus();
        disconnectDevice.setConnectYn("N");
        disconnectDevice.setEqId(device.getEqId());
        disconnectDevice.setLastCommTime(device.getLastCommTime());
        session.update(disconnectDevice);
    }

    private String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return formatter.format(new Date());
    }
}
