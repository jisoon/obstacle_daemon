package com.neonex.detector;

import akka.actor.UntypedActor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.neonex.message.StartMsg;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqCpu;
import com.neonex.model.EqInfo;
import com.neonex.model.Event;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-17
 */
@Slf4j
public class CpuEventDetector extends UntypedActor {


    public static final String CPU_EVENT_CODE = "RES0001";

    /**
     * Actor message recive
     *
     * @param message
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            processCpuWatcher(((StartMsg) message).getEqIds());
        }
    }

    /**
     * 해당 장비에 대한 모델 코드를 조회 한다.
     * 모델 코드별로 설정된 임계치 정보를 조회 한다.
     * 해당 모델 코드의 임계치 정보와 장비의 CPU 임계치 정보를 비교 한다.
     * 해당 장비가 임계치를 초과 하였다면
     * 장애 이벤트를 insert 하는데 기존에 같은 등급의 장애가 존재 한다면 Skip
     *
     * @param eqIds
     */
    public void processCpuWatcher(final Collection<String> eqIds) {
        List<CompModelEvent> deviceModelThresHold = fetchCpuThresHold();
        final List<EqCpu> eqCpuStatus = findCpuStatusBy(eqIds);
        for (EqCpu eqCpu : eqCpuStatus) {
            final String eqModelCode = findEqModelCode(eqCpu.getEqId());
            final CompModelEvent threshold = findThresholdByModelCode(deviceModelThresHold, eqModelCode);

            // 임계치 범위 사이에 cpu 사용률이 존재 한다면
            if (isCpuUsageObstacle(eqCpu.getCpuUsage(), threshold)) {
                if (noHasEqualsCpuEventLevel(eqCpu.getEqId(), threshold.getEventLvCode())) {
                    // 기존 이벤트를 처리 완료 하고 새로운 이벤트 등록
                    initCpuEventByMsgUpdateEventLevel(eqCpu.getEqId());
                    insertCpuEvent(eqCpu.getEqId(), eqCpu.getCpuUsage(), threshold.getEventLvCode());
                }
            }
        }
    }



    public void initCpuEventByMsgUpdateEventLevel(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.getTransaction().begin();
        Query query = session.createSQLQuery(
                "update EQ_EVENT_LOG " +
                        "set PROCESS_YN = 'Y', PROCESS_CONT = :processCont " +
                        "where EQ_ID = :eqId " +
                        "and EVENT_CODE = :eventCode"
        );
        query.setParameter("eqId", eqId);
        query.setParameter("eventCode", CPU_EVENT_CODE);
        query.setParameter("processCont", "등급 변경");
        query.executeUpdate();
        session.getTransaction().commit();
        session.close();
    }

    public String findEqModelCode(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        EqInfo eqInfo = session.get(EqInfo.class, eqId);
        return eqInfo.getEqModel().getModelCode();
    }


    /**
     * CPU 임계치 정보 조회
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<CompModelEvent> fetchCpuThresHold() {

        Session session = HibernateUtils.getSessionFactory().openSession();
        List<CompModelEvent> thresholdList = session.createCriteria(CompModelEvent.class)
                .add(Restrictions.eq("eventCode", CPU_EVENT_CODE))
                .list();
        return thresholdList;
    }

    /**
     * CPU 상태 정보 조회
     * @param eqIdList
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<EqCpu> findCpuStatusBy(Collection<String> eqIdList) {

        Session session = HibernateUtils.getSessionFactory().openSession();

        return session.createCriteria(EqCpu.class)
                .setProjection(
                        Projections.projectionList()
                                .add(Projections.groupProperty("eqId").as("eqId"))
                                .add(Projections.avg("cpuUsage").as("cpuUsage"))
                )
                .add(Restrictions.in("eqId", eqIdList))
                .setResultTransformer(Transformers.aliasToBean(EqCpu.class))
                .list();

    }

    /**
     * CPU 장애 감지
     * @param cpuUsage
     * @param threshold
     * @return
     */
    public boolean isCpuUsageObstacle(Double cpuUsage, CompModelEvent threshold) {
        return cpuUsage >= threshold.getMinValue() && cpuUsage < threshold.getMaxValue();
    }

    /**
     * CPU 장애 이벤트 추가     * @param eqId
     * @param cpuUsage
     * @param eventLvCode
     * @return
     */
    public boolean insertCpuEvent(String eqId, Double cpuUsage, String eventLvCode) {
        log.info("==== {} desvice insert cpu obstacle event", eqId);
        Session session = HibernateUtils.getSessionFactory().openSession();

        Event event = createCpuEventLog(eqId, cpuUsage, eventLvCode);
        event.setEventSeq(getEventSeq(session));
        try {
            // 이미 있는 CPU 장애 라면
            // 등급이 다른 경우 이전 등급은 처리 하고 새로 등급 insert
            // 등급이 같은 경우 Skip
            session.getTransaction().begin();
            session.save(event);
            session.getTransaction().commit();
            return true;
        } catch (HibernateException e) {
            log.error("cpu event insert error", e);
            session.getTransaction().rollback();
            return false;
        } finally {
            session.close();
        }
    }

    private Event createCpuEventLog(String eqId, Double cpuUsage, String eventLvCode) {
        Event event = new Event();
        event.setEqId(eqId);
        event.setEventCode(CPU_EVENT_CODE);
        event.setEventCont("CPU 사용률 " + cpuUsage + "%");
        event.setEventLevelCode(eventLvCode);
        event.setProcessYn("N");
        event.setOccurDate(currentTime());
        return event;
    }

    @SuppressWarnings("SqlDialectInspection")
    private Long getEventSeq(Session session) {
        Query query = session.createSQLQuery("select SQNT_EQ_EVENT_LOG_SEQ.nextval from dual");
        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    private String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return formatter.format(new Date());
    }

    public boolean noHasEqualsCpuEventLevel(String testEqId, String eventLevelCode) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        List<Event> events = session.createCriteria(Event.class)
                .add(Restrictions.eq("eqId", testEqId))
                .add(Restrictions.eq("eventCode", CPU_EVENT_CODE))
                .add(Restrictions.eq("eventLevelCode", eventLevelCode))
                .add(Restrictions.eq("processYn", "N"))
                .list();
        session.close();
        log.info("event log size {} ", events.size());
        return events.size() == 0;
    }

    private CompModelEvent findThresholdByModelCode(List<CompModelEvent> deviceModelThresHold, final String eqModelCode) {
        return Iterables.find(deviceModelThresHold, new Predicate<CompModelEvent>() {
            public boolean apply(CompModelEvent threHold) {
                return threHold.getModelCode().equals(eqModelCode);
            }
        });
    }
}