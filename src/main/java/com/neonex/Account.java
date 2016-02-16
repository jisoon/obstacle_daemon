package com.neonex;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
public class Account {

    @Id
    @Column(name="ACCOUNT_ID")
    private String accountId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


}
