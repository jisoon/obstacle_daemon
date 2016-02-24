package com.neonex.message;

import com.neonex.model.EqStatus;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author : 지순
 * @packageName : com.neonex.message
 * @since : 2016-02-16
 */
public class StartMsg implements Serializable {

    private Collection<EqStatus> eqStatusList;

    public Collection<EqStatus> getEqStatusList() {
        return eqStatusList;
    }

    public void setEqStatusList(Collection<EqStatus> eqStatusList) {
        this.eqStatusList = eqStatusList;
    }
}
