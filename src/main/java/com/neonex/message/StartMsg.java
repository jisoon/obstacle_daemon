package com.neonex.message;

import com.neonex.dto.EqStatus;

import java.io.Serializable;
import java.util.List;

/**
 * @author : 지순
 * @packageName : com.neonex.message
 * @since : 2016-02-16
 */
public class StartMsg implements Serializable {

    private List<EqStatus> eqStatusList;

    public List<EqStatus> getEqStatusList() {
        return eqStatusList;
    }

    public void setEqStatusList(List<EqStatus> eqStatusList) {
        this.eqStatusList = eqStatusList;
    }
}
