package com.neonex.watchers;

import akka.actor.UntypedActor;
import com.neonex.message.StartMsg;
import com.neonex.model.EqStatus;
import com.neonex.model.EventLog;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-17
 */
@Slf4j
public class DisconnectWatcher extends UntypedActor {

    public static final String DICONNECT_EVENT_CODE = "CON0002";
    public static final String DISCONNECT_EVENT_CON = "미연결";

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("==== DisconnectWatcher message receive");
        if (message instanceof StartMsg) {

            // 장비 상태를 미연결로 변경
            Collection<String> eqIds = ((StartMsg) message).getEqIds();

            updateStatusDisconnect(eqIds);
            initObstalceEvent(eqIds);
            insertDisconnectEvent(eqIds);

            log.info("==== DisconnectWatcher done!!!");
        }
    }

    /**
     * 장비 미연결 상태로 update
     * transaction 때문에 session 을 파라미터로 받아 됩니다.
     *
     * @param eqIds
     */
    public void updateStatusDisconnect(Collection<String> eqIds) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.getTransaction().begin();
        for (String eqId : eqIds) {
            EqStatus disconnectDevice = new EqStatus();
            disconnectDevice.setConnectYn("N");
            disconnectDevice.setEqId(eqId);
            session.merge(disconnectDevice);
        }
        session.getTransaction().commit();
        session.close();

    }

    public void initObstalceEvent(Collection<String> eqIds) {
        long startTime = System.currentTimeMillis();

        Session session = HibernateUtils.getSessionFactory().openSession();
        session.getTransaction().begin();
        try {
            for (String eqId : eqIds) {
                EventLog eventLog = new EventLog();
                eventLog.setEqId(eqId);
                eventLog.setProcessDate(currentTime());
                eventLog.setProcessYn("Y");
                eventLog.setProcessCont("장비 미연결로 인한 이벤트 초기화");


                Query query = session.createSQLQuery(
                        "update EQ_EVENT_LOG " +
                                "set PROCESS_YN = 'Y', PROCESS_CONT = :processCont " +
                                "where EQ_ID = :eqId " +
                                "and EVENT_CODE not in('CON0001','CON0002')"
                );
                query.setParameter("eqId", eqId);
                query.setParameter("processCont", "미연결로 인한 이벤트 초기화");
                query.executeUpdate();
            }
            session.getTransaction().commit();
            session.close();
        } catch (Exception e) {
            log.error("init obstacle event error", e);
            session.getTransaction().rollback();

        }
    }

    public void insertDisconnectEvent(Collection<String> eqIds) {
        for (String eqId : eqIds) {
            // 이미 미연결 장애 이벤트가 있다면 skip
            if (hasNoDisconnectionEvent(eqId)) {
                insertDisconnectEventLog(eqId);
            }
        }

    }

    private String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return formatter.format(new Date());
    }

    /**
     * 미연결 이벤트 insert
     * transaction 때문에 session 을 파라미터로 받아 됩니다.
     *
     * @param eqId
     * @return
     */
    public void insertDisconnectEventLog(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();

        EventLog eventLog = new EventLog();
        eventLog.setEqId(eqId);
        eventLog.setEventCode(DICONNECT_EVENT_CODE);
        eventLog.setEventCont(DISCONNECT_EVENT_CON);
        eventLog.setEventLevelCode("CRITICAL");
        eventLog.setProcessYn("N");
        eventLog.setOccurDate(currentTime());
        eventLog.setEventSeq(getEventSeq(session));
        try {
            session.save(eventLog);
        } catch (Exception e) {
            log.error("insert disconnect device error", e);
        }
    }

    private Long getEventSeq(Session session) {
        Query query = session.createSQLQuery("select SQNT_EQ_EVENT_LOG_SEQ.nextval from dual");
        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /**
     * 미연결 이벤트가 존재 하는지 확인
     *
     * @param eqId
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean hasNoDisconnectionEvent(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();

        List<EventLog> disconnEvent = session.createCriteria(EventLog.class)
                .add(Restrictions.eq("eqId", eqId))
                .add(Restrictions.eq("processYn", "N"))
                .add(Restrictions.eq("eventCode", "CON0002")).list();
        session.close();
        return disconnEvent.size() > 0 ? false : true;
    }
}
