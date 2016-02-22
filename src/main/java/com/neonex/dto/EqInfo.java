package com.neonex.dto;

import javax.persistence.*;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-22
 */
@Entity
@Table(name = "EQ_INFO")
public class EqInfo {

    @Id
    @Column(name = "EQ_ID")
    private String eqId;

    @Column(name = "EQ_NM")
    private String eqNm;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "MODEL_CODE")
    private EqModel eqModel;

    public String getEqId() {
        return eqId;
    }

    public void setEqId(String eqId) {
        this.eqId = eqId;
    }

    public String getEqNm() {
        return eqNm;
    }

    public void setEqNm(String eqNm) {
        this.eqNm = eqNm;
    }

    public EqModel getEqModel() {
        return eqModel;
    }

    public void setEqModel(EqModel eqModel) {
        this.eqModel = eqModel;
    }
}
