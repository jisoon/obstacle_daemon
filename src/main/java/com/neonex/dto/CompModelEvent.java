package com.neonex.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-19
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "COMP_MODEL_EVENT")
public class CompModelEvent {

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

    public String getCompModelEventSeq() {
        return compModelEventSeq;
    }

    public void setCompModelEventSeq(String compModelEventSeq) {
        this.compModelEventSeq = compModelEventSeq;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventCont() {
        return eventCont;
    }

    public void setEventCont(String eventCont) {
        this.eventCont = eventCont;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public String getNoticeMethod() {
        return noticeMethod;
    }

    public void setNoticeMethod(String noticeMethod) {
        this.noticeMethod = noticeMethod;
    }

    public String getEventLvCode() {
        return eventLvCode;
    }

    public void setEventLvCode(String eventLvCode) {
        this.eventLvCode = eventLvCode;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getCompCode() {
        return compCode;
    }

    public void setCompCode(String compCode) {
        this.compCode = compCode;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }
}
