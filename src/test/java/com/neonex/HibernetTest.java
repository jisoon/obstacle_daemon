package com.neonex;

import com.neonex.dto.Account;
import junit.framework.TestCase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import java.util.List;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
public class HibernetTest extends TestCase{


    @Test
    public void testConnectionTest() throws Exception {
        // given
        Session session = getSessionFactory().openSession();

        // when

        List<Account> accounts = (List<Account>)session.createCriteria(Account.class).list();

        System.out.printf(""+accounts.size());

        session.close();

        // then
        assertTrue(accounts.size() > 0);

    }

    private SessionFactory getSessionFactory() {

        return new Configuration().configure().buildSessionFactory();
    }
}
