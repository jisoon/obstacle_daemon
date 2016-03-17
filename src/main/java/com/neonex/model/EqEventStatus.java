package com.neonex.model;

import lombok.Data;

/**
 * @author : 지순
 * @packageName : com.neonex.model
 * @since : 2016-03-17
 */
@Data
public class EqEventStatus {
    private String viewStatus;
    private String preStatus;
    private String currStatus;
    private int obsCount;

}
