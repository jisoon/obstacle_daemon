package com.neonex.dto;

import javax.persistence.*;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-17
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "EQ_STATUS_INFO")
public class EqStatus {

    @Id
    @Column(name = "EQ_ID")
    private String eqId;

    @Column(name = "LAST_COMM_TIME")
    private String lastCommTime;

    @Column(name = "CONNECT_YN")
    private String connectYn;

    @OneToOne
    @JoinColumn(name = "EQ_ID")
    private EqInfo eqInfo;

    public String getEqId() {
        return eqId;
    }

    public void setEqId(String eqId) {
        this.eqId = eqId;
    }

    public String getLastCommTime() {
        return lastCommTime;
    }

    public void setLastCommTime(String lastCommTime) {
        this.lastCommTime = lastCommTime;
    }

    public String getConnectYn() {
        return connectYn;
    }

    public void setConnectYn(String connectYn) {
        this.connectYn = connectYn;
    }

    public EqInfo getEqInfo() {
        return eqInfo;
    }

    public void setEqInfo(EqInfo eqInfo) {
        this.eqInfo = eqInfo;
    }
}
