package com.neonex.model;

import lombok.Data;

import javax.persistence.*;

/**
 * @author : 지순
 * @packageName : com.neonex.dto
 * @since : 2016-02-22
 */
@Data
@Entity
@Table(name = "EQ_INFO")
public class EqInfo {

    @Id
    @Column(name = "EQ_ID")
    private String eqId;

    @Column(name = "EQ_NM")
    private String eqNm;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "MODEL_CODE")
    private EqModel eqModel;
}
