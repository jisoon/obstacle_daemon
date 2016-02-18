package com.neonex.dto;


import javax.persistence.*;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-18
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name="EQ_EVENT_LOG")
@SequenceGenerator(name="eventLogSeqGenerator", sequenceName="SQNT_EQ_EVENT_LOG_SEQ")
public class EventLog {
    @Id
    @Column(name="EVENT_SEQ")
    private Long eventSeq;

    @Column(name="EQ_ID")
    private String eqId;

    @Column(name="EVENT_CONT")
    private String eventCont;

    @Column(name="OCCUR_DATE")
    private String occurDate;

    @Column(name="PROCESS_YN")
    private String processVn;

    @Column(name="PROCESS_DATE")
    private String processDate;

    @Column(name="EVENT_LV")
    private int eventLv;


    public Long getEventSeq() {
        return eventSeq;
    }

    public void setEventSeq(Long eventSeq) {
        this.eventSeq = eventSeq;
    }

    public String getEqId() {
        return eqId;
    }

    public void setEqId(String eqId) {
        this.eqId = eqId;
    }

    public String getEventCont() {
        return eventCont;
    }

    public void setEventCont(String eventCont) {
        this.eventCont = eventCont;
    }

    public String getOccurDate() {
        return occurDate;
    }

    public void setOccurDate(String occurDate) {
        this.occurDate = occurDate;
    }

    public String getProcessVn() {
        return processVn;
    }

    public void setProcessVn(String processVn) {
        this.processVn = processVn;
    }

    public String getProcessDate() {
        return processDate;
    }

    public void setProcessDate(String processDate) {
        this.processDate = processDate;
    }

    public int getEventLv() {
        return eventLv;
    }

    public void setEventLv(int eventLv) {
        this.eventLv = eventLv;
    }

    @Override
    public String toString() {
        return "EventLog{" +
                "eventSeq=" + eventSeq +
                ", eqId='" + eqId + '\'' +
                ", eventCont='" + eventCont + '\'' +
                ", occurDate='" + occurDate + '\'' +
                ", processVn='" + processVn + '\'' +
                ", processDate='" + processDate + '\'' +
                ", eventLv=" + eventLv +
                '}';
    }
}
