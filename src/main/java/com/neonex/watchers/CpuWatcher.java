package com.neonex.watchers;

import akka.actor.UntypedActor;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.neonex.message.StartMsg;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqCpu;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;


/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-17
 */
public class CpuWatcher extends UntypedActor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Collection<EqStatus> eqStatusList;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            eqStatusList = ((StartMsg) message).getEqStatusList();
            logger.info("eq status list size {}", eqStatusList.size());
        }
    }

    public List<CompModelEvent> fetchCpuThresHold() {

        Session session = HibernateUtils.getSessionFactory().openSession();
        List<CompModelEvent> thresholdList = session.createCriteria(CompModelEvent.class)
                .add(Restrictions.eq("eventCode", "RES0001"))
                .list();
        logger.info("thresholdList {}", thresholdList);
        return thresholdList;
    }

    public List<EqCpu> fetchEqCpuStatus() {

        Session session = HibernateUtils.getSessionFactory().openSession();

        Collection<String> eqIdList = Collections2.transform(eqStatusList, new Function<EqStatus, String>() {
            @Override
            public String apply(EqStatus eqStatus) {
                return eqStatus.getEqId();
            }
        });

        List<EqCpu> eqCpus = session.createCriteria(EqCpu.class).add(Restrictions.in("eqId", eqIdList)).list();

//        List<EqCpu> eqCpus = new ArrayList<EqCpu>();
//        EqCpu eqCpu = new EqCpu();
//        eqCpu.setEqId("1");
//        eqCpu.setCpuUsage(60);
//        eqCpu.setCoreNum(1);
//        eqCpu.setCpuModel("INTEL");
//        eqCpus.add(eqCpu);
        return eqCpus;
    }

    public Collection<EqStatus> getEqStatusList() {
        return eqStatusList;
    }

    public void setEqStatusList(Collection<EqStatus> eqStatusList) {
        this.eqStatusList = eqStatusList;
    }


}
