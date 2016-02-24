package com.neonex.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-22
 */
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

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getModelNm() {
        return modelNm;
    }

    public void setModelNm(String modelNm) {
        this.modelNm = modelNm;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getOsVer() {
        return osVer;
    }

    public void setOsVer(String osVer) {
        this.osVer = osVer;
    }

    public String getDisconnectObstacleYn() {
        return disconnectObstacleYn;
    }

    public void setDisconnectObstacleYn(String disconnectObstacleYn) {
        this.disconnectObstacleYn = disconnectObstacleYn;
    }

    public String getMakerNum() {
        return makerNum;
    }

    public void setMakerNum(String makerNum) {
        this.makerNum = makerNum;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }
}
