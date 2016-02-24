package com.neonex.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-22
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@Entity
@Table(name = "EQ_MODEL_INFO")
public class EqModel {

    @Id
    @Column(name = "MODEL_CODE")
    private String modelCode;

    @Column(name = "MODEL_NM")
    private String modelNm;

    @Column(name = "OS_TYPE")
    private String osType;

    @Column(name = "OS_VER")
    private String osVer;

    @Column(name = "DISCONNECT_OBSTACLE_YN")
    private String disconnectObstacleYn;

    @Column(name = "MAKER_NUM")
    private String makerNum;

    @Column(name = "TYPE_CODE")
    private String typeCode;
}
