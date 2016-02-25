package com.neonex.watchers;

import akka.actor.UntypedActor;
import com.neonex.message.StartMsg;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqCpu;
import com.neonex.model.EventLog;
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
public class CpuWatcher extends UntypedActor {


    public static final String CPU_EVENT_CODE = "RES0001";
    private Collection<String> eqIds;

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("cpuWatcher message receive");
        if (message instanceof StartMsg) {
            log.info("cpuWatcher start message receive");
            eqIds = ((StartMsg) message).getEqIds();
            log.info("eq status list size {}", eqIds.size());
            List<CompModelEvent> compModelEventList = fetchCpuThresHold();
            List<EqCpu> eqCpus = fetchEqCpuStatus(eqIds);
        }
    }

    public List<CompModelEvent> fetchCpuThresHold() {

        Session session = HibernateUtils.getSessionFactory().openSession();
        List<CompModelEvent> thresholdList = session.createCriteria(CompModelEvent.class)
                .add(Restrictions.eq("eventCode", CPU_EVENT_CODE))
                .list();
        log.info("thresholdList {}", thresholdList);
        return thresholdList;
    }

    public List<EqCpu> fetchEqCpuStatus(Collection<String> eqIdList) {

        Session session = HibernateUtils.getSessionFactory().openSession();

        Criteria crit = session.createCriteria(EqCpu.class);
        List<EqCpu> eqCpus = crit.setProjection(
                Projections.projectionList()
                        .add(Projections.groupProperty("eqId").as("eqId"))
                        .add(Projections.avg("cpuUsage").as("cpuUsage"))
        )
                .add(Restrictions.in("eqId", eqIdList))
                .setResultTransformer(Transformers.aliasToBean(EqCpu.class)) // alias 랑 이거 없으면 List<Object> 로 리턴함
                .list();
        return eqCpus;
    }

    public boolean detectCpuObstacle(Double cpuUsage, Double cpuMinThresHold, Double cpuMaxThresHold) {
        return cpuUsage >= cpuMinThresHold && cpuUsage < cpuMaxThresHold ? true : false;
    }

    public boolean insertEvent(String eqId, Double cpuUsage) {
        Session session = HibernateUtils.getSessionFactory().openSession();

        EventLog eventLog = createCpuEventLog(eqId, cpuUsage);
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

    private EventLog createCpuEventLog(String eqId, Double cpuUsage) {
        EventLog eventLog = new EventLog();
        eventLog.setEqId(eqId);
        eventLog.setEventCode(CPU_EVENT_CODE);
        eventLog.setEventCont("CPU 사용률 " + cpuUsage + "%");
        eventLog.setEventLv(1);
        eventLog.setProcessYn("N");
        eventLog.setOccurDate(currentTime());
        return eventLog;
    }

    private Long getEventSeq(Session session) {
        Query query = session.createSQLQuery("select SQNT_EQ_EVENT_LOG_SEQ.nextval from dual");
        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    private String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return formatter.format(new Date());
    }
}
