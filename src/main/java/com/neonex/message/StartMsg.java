package com.neonex.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author : 지순
 * @packageName : com.neonex.message
 * @since : 2016-02-16
 */
@Data
@AllArgsConstructor
public class StartMsg implements Serializable {

    public StartMsg() {

    }

    private Collection<String> eqIds;
}
