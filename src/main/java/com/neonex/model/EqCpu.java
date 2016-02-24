package com.neonex.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : 지순
 * @packageName : com.neonex.model
 * @since : 2016-02-24
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "EQ_CPU")
public class EqCpu {

    @Column(name = "CPU_USAGE")
    private int cpuUsage;

    @Id
    @Column(name = "EQ_ID")
    private String eqId;

    @Column(name = "CPU_MODEL")
    private String cpuModel;

    @Column(name = "CPU_MAKER")
    private String cpuMaker;

    @Column(name = "CORE_NUM")
    private int coreNum;

    public int getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(int cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public String getEqId() {
        return eqId;
    }

    public void setEqId(String eqId) {
        this.eqId = eqId;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public String getCpuMaker() {
        return cpuMaker;
    }

    public void setCpuMaker(String cpuMaker) {
        this.cpuMaker = cpuMaker;
    }

    public int getCoreNum() {
        return coreNum;
    }

    public void setCoreNum(int coreNum) {
        this.coreNum = coreNum;
    }

    @Override
    public String toString() {
        return "EqCpu{" +
                "cpuUsage=" + cpuUsage +
                ", eqId='" + eqId + '\'' +
                ", cpuModel='" + cpuModel + '\'' +
                ", cpuMaker='" + cpuMaker + '\'' +
                ", coreNum=" + coreNum +
                '}';
    }
}
