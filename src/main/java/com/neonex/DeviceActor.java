package com.neonex;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.neonex.message.StartMsg;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqStatus;
import com.neonex.model.EventLog;
import com.neonex.utils.HibernateUtils;
import com.neonex.watchers.CpuWatcher;
import com.neonex.watchers.MemWatcher;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@Slf4j
@SuppressWarnings("JpaQlInspection")
public class DeviceActor extends UntypedActor {

    public static final String DICONNECT_EVENT_CODE = "CON0002";
    public static final String DISCONNECT_EVENT_CON = "미연결";
    private ActorRef cpuWatcher;

    private ActorRef memWatcher;

    public DeviceActor() {
        log.info("default construnctor");
        cpuWatcher = context().actorOf(Props.create(CpuWatcher.class), "cpuWatcher");
        memWatcher = context().actorOf(Props.create(MemWatcher.class), "memWatcher");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            log.info("=== message received!!! ===");
            List<EqStatus> devices = fetchDevice();
            detectDisconnect(devices);
            // device connection 상태가 N 인 장비는 제거하고
            // eqId 만 존재 하는 collection 으로 변경
            ((StartMsg) message).setEqIds(convertEqIdCollection(filterConnectionDevice(devices)));

            cpuWatcher.tell(message, getSelf());

        } else {
            unhandled(message);
        }
    }

    private Collection<EqStatus> filterConnectionDevice(List<EqStatus> devices) {
        return Collections2.filter(
                devices, new Predicate<EqStatus>() {
                    @Override
                    public boolean apply(EqStatus devices) {
                        return Objects.equal(devices.getConnectYn(), "Y");
                    }
                });
    }

    /**
     * 현재 연결된 장비들의 상태 정보 조회
     *
     * @return
     */
    public List<EqStatus> fetchDevice() {
        log.info("=== fetchDevice ===");
        Session session = HibernateUtils.getSessionFactory().openSession();
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
    public void detectDisconnect(List<EqStatus> devices) {
        log.info("=== detectDisconnect === ");

        // 미연결 임계치 조회
        // 장비의 모델 코드로 미연결 임계치 정보를 조회 할 수 있음
        Map<String, Object> thresHold = fetchConnetionThresHold();

        Session session = HibernateUtils.getSessionFactory().openSession();
        session.getTransaction().begin();
        try {
            for (EqStatus device : devices) {
                String deviceModelCode = device.getEqInfo().getEqModel().getModelCode();

                // 장비의 미연결 임계치가 존재 하면 미연결 여부를 체크 하고
                // 존재 하지 않는다면 skip 함
                if (thresHold.containsKey(deviceModelCode)) {

                    // 장비 모델 코드로 미연결 인터벌 값을 조회
                    int connectionInterval = (Integer) thresHold.get(device.getEqInfo().getEqModel().getModelCode());

                    long deviceLastConnTime = 0L;
                    if (Strings.isNullOrEmpty(device.getLastCommTime())) {
                        deviceLastConnTime = Long.parseLong(device.getLastCommTime());
                    }
                    log.info(">>>> device last connection time {} ", deviceLastConnTime);

                    long thresholdLastCommTime = calcConnectionThresHoldTime(connectionInterval);
                    log.info(">>>> device threshold connection time {} ", thresholdLastCommTime);

                    // DB 시간과 임계치 시간 비교
                    if (deviceLastConnTime < thresholdLastCommTime) {
                        log.info(">>>> device status disconnect");

                        // 장비 상태를 미연결로 변경
                        updateStatusDisconnect(session, device);

                        // 기존에 있던 장애 이벤트를 초기화화
                        initObstalceEvent(device.getEqId());

                        // 이미 미연결 장애 이벤트가 있다면 skip
                        if (hasNoDisconnectionEvent(device.getEqId())) {
                            insertDisconnectEvent(session, device.getEqId(), "CRITICAL");
                        }
                        // device list 상태를 N 으로 변경
                        device.setConnectYn("N");
                    }
                }
            }
        } catch (NumberFormatException ne) {
            session.getTransaction().rollback();
        } catch (Exception e) {
            log.info("Exception ");
            e.printStackTrace();
        }
        session.getTransaction().commit();
        session.close();
    }

    /**
     * 미연결 이벤트를 제외한 이벤트 처리 완료
     *
     * @param eqId
     * @return
     */
    public boolean initObstalceEvent(String eqId) {
        long startTime = System.currentTimeMillis();

        Session session = HibernateUtils.getSessionFactory().openSession();

        EventLog eventLog = new EventLog();
        eventLog.setEqId(eqId);
        eventLog.setProcessDate(currentTime());
        eventLog.setProcessYn("Y");
        eventLog.setProcessCont("장비 미연결로 인한 이벤트 초기화");
        try {
            session.getTransaction().begin();
            Query query = session.createSQLQuery(
                    "update EQ_EVENT_LOG " +
                            "set PROCESS_YN = 'Y', PROCESS_CONT = :processCont " +
                            "where EQ_ID = :eqId " +
                            "and EVENT_CODE not in('CON0001','CON0002')"
            );
            query.setParameter("eqId", eqId);
            query.setParameter("processCont", "미연결로 인한 이벤트 초기화");
            query.executeUpdate();
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            log.error("init obstacle event error", e);
            session.getTransaction().rollback();
            return false;
        } finally {

            long endTime = System.currentTimeMillis();
            log.info("Total elapsed time = " + (endTime - startTime));
            session.close();
        }

        //13:43:00 INFO  com.neonex.DeviceActor - Total elapsed time = 256

    }

    /**
     * 미연결 이벤트가 존재 하는지 확인
     * @param eqId
     * @return
     */
    public boolean hasNoDisconnectionEvent(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        List<EventLog> disconnEvent = session.createCriteria(EventLog.class)
                .add(Restrictions.eq("processYn", "N"))
                .add(Restrictions.eq("eventCode", "CON0002")).list();
        return disconnEvent.size() > 0 ? false : true;
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

    /**
     * 장비 상태 조회
     * @param eqId
     * @return
     */
    public EqStatus findDevice(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        EqStatus device = session.get(EqStatus.class, eqId);
        session.close();
        return device;
    }

    /**
     * 장비 미연결 상태로 update
     * transaction 때문에 session 을 파라미터로 받아 됩니다.
     * @param session
     * @param device
     */
    public void updateStatusDisconnect(Session session, EqStatus device) {
        log.info("=== updateStatusDisconnect ===");
        EqStatus disconnectDevice = new EqStatus();
        disconnectDevice.setConnectYn("N");
        disconnectDevice.setEqId(device.getEqId());
        disconnectDevice.setLastCommTime(device.getLastCommTime());
        session.update(disconnectDevice);
    }

    /**
     * 미연결 이벤트 insert
     * transaction 때문에 session 을 파라미터로 받아 됩니다.
     * @param session
     * @param eqId
     * @return
     */
    public boolean insertDisconnectEvent(Session session, String eqId, String eventLevelCode) {
        log.info("=== insertDisconnectEvent ===");
        EventLog eventLog = new EventLog();
        eventLog.setEqId(eqId);
        eventLog.setEventCode(DICONNECT_EVENT_CODE);
        eventLog.setEventCont(DISCONNECT_EVENT_CON);
        eventLog.setEventLevelCode(eventLevelCode);
        eventLog.setProcessYn("N");
        eventLog.setOccurDate(currentTime());
        eventLog.setEventSeq(getEventSeq(session));
        try {
            session.save(eventLog);
            return true;
        } catch (Exception e) {
            log.error("insert disconnect device error", e);
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

    private Collection<String> convertEqIdCollection(Collection<EqStatus> eqStatusList) {
        return Collections2.transform(eqStatusList, new Function<EqStatus, String>() {
            @Override
            public String apply(EqStatus eqStatus) {
                return eqStatus.getEqId();
            }
        });
    }
}
