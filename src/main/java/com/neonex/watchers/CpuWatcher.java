package com.neonex.watchers;

import akka.actor.UntypedActor;
import com.neonex.message.StartMsg;
import com.neonex.model.*;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
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
public class CpuWatcher extends UntypedActor {


    public static final String CPU_EVENT_CODE = "RES0001";

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("==== CpuWatcher message receive");
        if (message instanceof StartMsg) {

            Collection<String> eqIds = ((StartMsg) message).getEqIds();

            List<CompModelEvent> compModelEventList = fetchCpuThresHold();
            List<EqCpu> eqCpus = fetchEqCpuStatus(eqIds);
            for (EqCpu eqCpu : eqCpus) {
                for (CompModelEvent threshold : compModelEventList) {
                    String eqModelCode = findEqModelCode(eqCpu.getEqId());
                    if (Objects.equals(eqModelCode, threshold.getModelCode())) {
                        if (detectCpuObstacle(eqCpu.getCpuUsage(), threshold.getMinValue(), threshold.getMaxValue())) {
                            insertEvent(eqCpu.getEqId(), eqCpu.getCpuUsage(), threshold.getEventLvCode());
                        }
                    }
                }
            }
            log.info("==== cpuWatcher done!!!");
        }
    }

    public String findEqModelCode(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        EqInfo eqInfo = session.get(EqInfo.class, eqId);
        return eqInfo.getEqModel().getModelCode();
    }


    @SuppressWarnings("unchecked")
    public List<CompModelEvent> fetchCpuThresHold() {

        Session session = HibernateUtils.getSessionFactory().openSession();
        List<CompModelEvent> thresholdList = session.createCriteria(CompModelEvent.class)
                .add(Restrictions.eq("eventCode", CPU_EVENT_CODE))
                .list();
        return thresholdList;
    }

    @SuppressWarnings("unchecked")
    public List<EqCpu> fetchEqCpuStatus(Collection<String> eqIdList) {

        Session session = HibernateUtils.getSessionFactory().openSession();


        Criteria crit = session.createCriteria(EqCpu.class);
//        crit = crit.createCriteria("eqCpu", JoinType.LEFT_OUTER_JOIN);
//                .setProjection(
//                        Projections.projectionList()
//                                .add(Projections.groupProperty("eqId").as("eqId"))
//                                .add(Projections.avg("cpuUsage").as("cpuUsage"))
//                )
//                .add(Restrictions.in("eqId", eqIdList))
//                .setResultTransformer(Transformers.aliasToBean(EqCpu.class));

//        return crit.list();
        return crit
                .setProjection(
                        Projections.projectionList()
                        .add(Projections.groupProperty("eqId").as("eqId"))
                        .add(Projections.avg("cpuUsage").as("cpuUsage"))
                )
                .add(Restrictions.in("eqId", eqIdList))
                .setResultTransformer(Transformers.aliasToBean(EqCpu.class))
                .list();

    }

    public boolean detectCpuObstacle(Double cpuUsage, int cpuMinThresHold, int cpuMaxThresHold) {
        return cpuUsage >= cpuMinThresHold && cpuUsage < cpuMaxThresHold;
    }

    public boolean insertEvent(String eqId, Double cpuUsage, String eventLvCode) {
        log.info("==== {} desvice insert cpu obstacle event", eqId);
        Session session = HibernateUtils.getSessionFactory().openSession();

        EventLog eventLog = createCpuEventLog(eqId, cpuUsage, eventLvCode);
        eventLog.setEventSeq(getEventSeq(session));
        try {
            session.getTransaction().begin();
            session.save(eventLog);
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

    private EventLog createCpuEventLog(String eqId, Double cpuUsage, String eventLvCode) {
        EventLog eventLog = new EventLog();
        eventLog.setEqId(eqId);
        eventLog.setEventCode(CPU_EVENT_CODE);
        eventLog.setEventCont("CPU 사용률 " + cpuUsage + "%");
        eventLog.setEventLevelCode(eventLvCode);
        eventLog.setProcessYn("N");
        eventLog.setOccurDate(currentTime());
        return eventLog;
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
}
