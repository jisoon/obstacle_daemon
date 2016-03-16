package com.neonex.model;


import lombok.Data;

import javax.persistence.*;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-18
 */
@Data
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name="EQ_EVENT_LOG")
@SequenceGenerator(name="eventLogSeqGenerator", sequenceName="SQNT_EQ_EVENT_LOG_SEQ")
public class Event {

    @Id
    @Column(name = "EVENT_SEQ")
    private Long eventSeq;

    @Column(name="EQ_ID")
    private String eqId;

    @Column(name="EVENT_CONT")
    private String eventCont;

    @Column(name="OCCUR_DATE")
    private String occurDate;

    @Column(name="PROCESS_YN")
    private String processYn;

    @Column(name="PROCESS_DATE")
    private String processDate;

    @Column(name = "EVENT_LV_CODE")
    private String eventLevelCode;

    @Column(name = "PROCESS_CONT")
    private String processCont;

    @Column(name = "EVENT_CODE")
    private String eventCode;
}
