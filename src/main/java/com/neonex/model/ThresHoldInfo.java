package com.neonex.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-19
 */
@Data
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "COMP_MODEL_EVENT")
public class ThresHoldInfo {

    @Id
    @Column(name = "COMP_MODEL_EVENT_SEQ")
    private String compModelEventSeq;

    @Column(name = "EVENT_CODE")
    private String eventCode;

    @Column(name = "EVENT_CONT")
    private String eventCont;

    @Column(name = "MIN_VALUE")
    private int minValue;

    @Column(name = "MAX_VALUE")
    private int maxValue;

    @Column(name = "NOTICE_METHOD")
    private String noticeMethod;

    @Column(name = "EVENT_LV_CODE")
    private String eventLvCode;

    @Column(name = "MODEL_CODE")
    private String modelCode;

    @Column(name = "COMP_CODE")
    private String compCode;

    @Column(name = "EVENT_TITLE")
    private String eventTitle;

    @Column(name = "USE_YN")
    private String useYn;

}
