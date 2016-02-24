package com.neonex.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : 지순
 * @packageName : com.neonex.model
 * @since : 2016-02-24
 */
@Data
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "EQ_CPU")
public class EqCpu {
    @Id
    @Column(name = "EQ_ID")
    private String eqId;

    @Column(name = "CPU_MODEL")
    private String cpuModel;

    @Column(name = "CPU_MAKER")
    private String cpuMaker;

    @Column(name = "CORE_NUM")
    private String coreNum;

    @Column(name = "CPU_USAGE")
    private double cpuUsage;
}


