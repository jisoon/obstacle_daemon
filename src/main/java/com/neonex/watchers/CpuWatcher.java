package com.neonex.watchers;

import akka.actor.UntypedActor;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.neonex.message.StartMsg;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqCpu;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import java.util.Collection;
import java.util.List;


/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-17
 */
@Slf4j
public class CpuWatcher extends UntypedActor {


    private Collection<EqStatus> eqStatusList;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            eqStatusList = ((StartMsg) message).getEqStatusList();
            log.info("eq status list size {}", eqStatusList.size());
        }
    }

    public List<CompModelEvent> fetchCpuThresHold() {

        Session session = HibernateUtils.getSessionFactory().openSession();
        List<CompModelEvent> thresholdList = session.createCriteria(CompModelEvent.class)
                .add(Restrictions.eq("eventCode", "RES0001"))
                .list();
        log.info("thresholdList {}", thresholdList);
        return thresholdList;
    }

    public List<EqCpu> fetchEqCpuStatus(List<EqStatus> eqStatusList) {

        Session session = HibernateUtils.getSessionFactory().openSession();

        Collection<String> eqIdList = Collections2.transform(eqStatusList, new Function<EqStatus, String>() {
            @Override
            public String apply(EqStatus eqStatus) {
                return eqStatus.getEqId();
            }
        });

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

    public boolean insertEvent(EqCpu eqCpu) {
        return false;
    }
}
