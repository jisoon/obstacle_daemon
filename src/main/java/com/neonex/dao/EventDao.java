package com.neonex.dao;

import com.neonex.model.Event;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author : 지순
 * @packageName : com.neonex.appender
 * @since : 2016-03-16
 */
@Slf4j
public class EventDao {
    /**
     * event save
     *
     * @param event
     * @return
     */
    public boolean save(Event event) {
        setDefaultValue(event);
        if (invaildEventData(event)) return false;
        if (saveEvent(event)) return true;
        return true;
    }

    private void setDefaultValue(Event event) {
        event.setProcessYn("N");
        event.setOccurDate(currentTime());
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
            return true;
        }
        if (StringUtils.isBlank(event.getEventLevelCode())) {
            log.error("eventLevelCode is blank");
            return true;
        }
        if (StringUtils.isBlank(event.getEventCode())) {
            log.error("eventCode is blank");
            return true;
        }
        if (StringUtils.isBlank(event.getOccurDate())) {
            log.error("occurDate is blank");
            return true;
        }
        if (StringUtils.isBlank(event.getEventCont())) {
            log.error("eventCont is blank");
            return true;
        }
        return false;
    }

    private Long createEventSeq() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        Query query = session.createSQLQuery("select SQNT_EQ_EVENT_LOG_SEQ.nextval from dual");
        long eventSeq = ((BigDecimal) query.uniqueResult()).longValue();
        session.close();
        return eventSeq;
    }

    private String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return formatter.format(new Date());
    }
}
