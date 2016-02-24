package com.neonex.model;

import lombok.Data;

import javax.persistence.*;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-17
 */
@Data
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

}
