package com.neonex.detector;

import akka.actor.UntypedActor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.neonex.message.StartMsg;
import com.neonex.model.*;
import com.neonex.dao.EventDao;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

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
    public static final String EVENT_ERROR_LEVEL_CODE = "ERROR";
    public static final String EVENT_NORMAL_LEVEL_CODE = "NORMAL";
    public static final int OCCURRENCE_COUNT = 1;

    private EventDao eventDao = new EventDao();

    /**
     * Actor message recive
     *
     * @param message
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            detect(((StartMsg) message).getEqIds());
        }
    }


    public List<Event> detect(final Collection<String> eqIds) {

        List<Event> events = new ArrayList<Event>();

        List<ThresHoldInfo> deviceModelThresHold = fetchCpuThresHold();
        final List<EqCpu> eqCpuStatus = findCpuStatusBy(eqIds);
        for (EqCpu eqCpu : eqCpuStatus) {
            final String eqModelCode = findEqModelCode(eqCpu.getEqId());
            final ThresHoldInfo threshold = filterByModelCode(deviceModelThresHold, eqModelCode);

            // 임계치 범위 사이에 cpu 사용률이 존재 한다면
            if (isCpuUsageObstacle(eqCpu.getCpuUsage(), threshold)) {
                events.add(createCpuEventLog(eqCpu.getEqId(), eqCpu.getCpuUsage(), threshold.getEventLvCode()));
//                if (noHasEqualsCpuEventLevel(eqCpu.getEqId(), threshold.getEventLvCode())) {
//                    eventDao.save();
//                } else {
//                    log.info("aleady cpu event-level");
//                }
            }
            // 기존 이벤트를 처리 완료 하고 새로운 이벤트 등록
//                    initCpuEventByMsgUpdateEventLevel(eqCpu.getEqId());
        }
        return events;
    }

    public boolean save(List<Event> events) {
        try {
            for (Event event : events) {
                eventDao.save(event);
            }
        } catch (Exception e) {
            log.error("cpu event save error");
            return false;
        }
        return true;
    }


//
//    public void initCpuEventByMsgUpdateEventLevel(String eqId) {
//        Session session = HibernateUtils.getSessionFactory().openSession();
//        session.getTransaction().begin();
//        Query query = session.createSQLQuery(
//                "update EQ_EVENT_LOG " +
//                        "set PROCESS_YN = 'Y', PROCESS_CONT = :processCont " +
//                        "where EQ_ID = :eqId " +
//                        "and EVENT_CODE = :eventCode"
//        );
//        query.setParameter("eqId", eqId);
//        query.setParameter("eventCode", CPU_EVENT_CODE);
//        query.setParameter("processCont", "등급 변경");
//        query.executeUpdate();
//        session.getTransaction().commit();
//        session.close();
//    }

    private String findEqModelCode(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        EqInfo eqInfo = session.get(EqInfo.class, eqId);
        return eqInfo.getEqModel().getModelCode();
    }


    /**
     * CPU 임계치 정보 조회
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<ThresHoldInfo> fetchCpuThresHold() {

        Session session = HibernateUtils.getSessionFactory().openSession();
        List<ThresHoldInfo> thresholdList = session.createCriteria(ThresHoldInfo.class)
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
    private List<EqCpu> findCpuStatusBy(Collection<String> eqIdList) {

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

    private boolean isCpuUsageObstacle(Double cpuUsage, ThresHoldInfo threshold) {
        return cpuUsage >= threshold.getMinValue() && cpuUsage < threshold.getMaxValue();
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

    private String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return formatter.format(new Date());
    }

//    private boolean noHasEqualsCpuEventLevel(String testEqId, String eventLevelCode) {
//        Session session = HibernateUtils.getSessionFactory().openSession();
//        List<Event> events = session.createCriteria(Event.class)
//                .add(Restrictions.eq("eqId", testEqId))
//                .add(Restrictions.eq("eventCode", CPU_EVENT_CODE))
//                .add(Restrictions.eq("eventLevelCode", eventLevelCode))
//                .add(Restrictions.eq("processYn", "N"))
//                .list();
//        session.close();
//        log.info("event log size {} ", events.size());
//        return events.size() == 0;
//    }

    /**
     * @param deviceModelThresHold
     * @param eqModelCode
     * @return
     */
    private ThresHoldInfo filterByModelCode(List<ThresHoldInfo> deviceModelThresHold, final String eqModelCode) {
        return Iterables.find(deviceModelThresHold, new Predicate<ThresHoldInfo>() {
            public boolean apply(ThresHoldInfo threHold) {
                return threHold.getModelCode().equals(eqModelCode);
            }
        });
    }


    public boolean isObstacleOccurrence(Event event) {
        EqEventStatus eqEventStatus = new EqEventStatus();
        eqEventStatus.setPreStatus(EVENT_ERROR_LEVEL_CODE);

        if (isPreStatusEquals(event.getEventLevelCode(), eqEventStatus.getPreStatus())) {
            eqEventStatus.setObsCount(eqEventStatus.getObsCount() + 1);
        } else {
            if (isViewStatusNormal(eqEventStatus.getViewStatus())) {
                eqEventStatus.setObsCount(eqEventStatus.getObsCount() + 1);
            } else {
                eqEventStatus.setObsCount(0);
            }
        }

        if (eqEventStatus.getObsCount() == OCCURRENCE_COUNT) {
            eqEventStatus.setViewStatus(event.getEventLevelCode());
            eqEventStatus.setObsCount(0);
            return true;
        }
        return false;
    }

    private boolean isViewStatusNormal(String viewStatus) {
        return StringUtils.equals(viewStatus, EVENT_NORMAL_LEVEL_CODE);
    }

    private boolean isPreStatusEquals(String eventLevelCode, String preStatus) {
        return StringUtils.equals(eventLevelCode, preStatus);
    }
}
