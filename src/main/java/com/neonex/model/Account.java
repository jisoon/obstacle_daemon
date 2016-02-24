package com.neonex.model;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@Data
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "ACCOUNT")
public class Account {

    @Id
    @Column(name="ACCOUNT_ID")
    private String accountId;

}
