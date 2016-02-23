package com.neonex.watchers;

import akka.actor.UntypedActor;
import com.neonex.dto.CompModelEvent;
import com.neonex.dto.EqStatus;
import com.neonex.message.StartMsg;
import com.neonex.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-17
 */
public class CpuWatcher extends UntypedActor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            List<EqStatus> eqStatusList = ((StartMsg) message).getEqStatusList();
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
}
