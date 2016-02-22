package com.neonex;

import akka.actor.UntypedActor;
import com.neonex.dto.CompModelEvent;
import com.neonex.dto.EqStatus;
import com.neonex.dto.EventLog;
import com.neonex.message.StartMsg;
import com.neonex.utils.HibernateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@SuppressWarnings("JpaQlInspection")
public class DeviceActor extends UntypedActor {

    private final static Logger logger = LoggerFactory.getLogger(DeviceActor.class);

    private SessionFactory sessionFactory;


    // !!!!!!!! 주의 !!!!!!!!!!!!!
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
            List<EqStatus> devices = fetchDevice();
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
    public List<EqStatus> fetchDevice() {
        logger.info("=== fetchDevice ===");
        Session session = sessionFactory.openSession();
        List<EqStatus> eqStatus = session.createCriteria(EqStatus.class)
                .add(Restrictions.eq("connectYn", "Y"))
                .list();
        session.close();
        return eqStatus;
    }

    /**
     * 미연결 장애 감지
     *
     * @param devices
     * @return
     */
    public int detectDisconnect(List<EqStatus> devices) {
        logger.info("=== detectDisconnect === ");

        // 미연결 임계치 조회
        // 장비의 모델 코드로 미연결 임계치 정보를 조회 할 수 있음
        Map<String, Object> thresHold = fetchConnetionThresHold();

        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        int disconnectCount = 0;
        try {
            for (EqStatus device : devices) {
                String deviceModelCode = device.getEqInfo().getEqModel().getModelCode();
                if (device.getLastCommTime() != null) {
                    if (thresHold.containsKey(deviceModelCode)) {
                        // 장비 모델 코드로 미연결 인터벌 값을 조회
                        int connectionInterval = (Integer) thresHold.get(device.getEqInfo().getEqModel().getModelCode());

                        long dbLastCommTime = Long.parseLong(device.getLastCommTime());
                        long thresholdLastCommTime = calcConnectionThresHoldTime(connectionInterval);
                        logger.info("thresholdLastCommTime {} ", thresholdLastCommTime);
                        logger.info("dbLastCommTime {} ", dbLastCommTime);

                        // DB 시간과 임계치 시간 비교
                        if (dbLastCommTime < thresholdLastCommTime) {
                            logger.info("disconnec target device : {} lastCommTime : {}", device.getEqId(), dbLastCommTime);

                            updateStatusDisconnect(session, device);

                            insertDisconnectEvent(session, device.getEqId());

                            disconnectCount++;
                        }
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

    /**
     * 연결 임계치 조회
     * 장비의 모델 코드로 연결 임계치를 조회 할 수 있도록 Map 으로 구성
     *
     * @return map
     */
    public Map<String, Object> fetchConnetionThresHold() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        List<CompModelEvent> thresholdList = session.createCriteria(CompModelEvent.class)
                .add(Restrictions.eq("eventCode", "CON0002"))
                .list();
        Map<String, Object> threshold = new HashMap<String, Object>();
        for (CompModelEvent event : thresholdList) {
            threshold.put(event.getModelCode(), event.getMaxValue());
        }
        session.close();
        return threshold;

    }

    public EqStatus findDevice(String eqId) {
        Session session = sessionFactory.openSession();
        EqStatus device = session.get(EqStatus.class, eqId);
        session.close();
        return device;
    }

    public void updateStatusDisconnect(Session session, EqStatus device) {
        logger.info("=== updateStatusDisconnect ===");
        EqStatus disconnectDevice = new EqStatus();
        disconnectDevice.setConnectYn("N");
        disconnectDevice.setEqId(device.getEqId());
        disconnectDevice.setLastCommTime(device.getLastCommTime());
        session.update(disconnectDevice);
    }

    public boolean insertDisconnectEvent(Session session, String eqId) {
        logger.info("=== insertDisconnectEvent ===");
        EventLog eventLog = new EventLog();
        eventLog.setEqId(eqId);
        eventLog.setEventCont("미연결");
        eventLog.setEventLv(1);
        eventLog.setProcessVn("N");
        eventLog.setOccurDate(currentTime());
        eventLog.setEventSeq(getEventSeq(session));
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

    private String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return formatter.format(new Date());
    }

    private Long calcConnectionThresHoldTime(int thresHoldTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, (int) ((-1) * thresHoldTime));
        return Long.parseLong(formatter.format(cal.getTime()));
    }
}
