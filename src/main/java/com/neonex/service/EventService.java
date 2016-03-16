package com.neonex.service;

import com.neonex.model.Event;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import java.math.BigDecimal;

/**
 * @author : 지순
 * @packageName : com.neonex.appender
 * @since : 2016-03-16
 */
@Slf4j
public class EventService {

    /**
     * event save
     *
     * @param event
     * @return
     */
    public boolean save(Event event) {
        if (invaildEventData(event)) return false;
        if (saveEvent(event)) return true;
        return true;
    }

    private boolean saveEvent(Event event) {
        try {
            event.setEventSeq(createEventSeq());

            Session session = HibernateUtils.getSessionFactory().openSession();
            session.getTransaction().begin();
            session.save(event);
            session.getTransaction().commit();
            session.close();
            return true;
        } catch (HibernateException e) {
            log.info("save exception", e);
            return false;

        }
    }

    private boolean invaildEventData(Event event) {

        if (StringUtils.isBlank(event.getEqId())) {
            log.error("eqId is blank");
            return false;
        }
        return true;
    }

    private Long createEventSeq() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        Query query = session.createSQLQuery("select SQNT_EQ_EVENT_LOG_SEQ.nextval from dual");
        long eventSeq = ((BigDecimal) query.uniqueResult()).longValue();
        session.close();
        return eventSeq;
    }
}
