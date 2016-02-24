package com.neonex.utils;

import com.neonex.model.Account;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author : 지순
 * @packageName : com.neonex.utils
 * @since : 2016-02-17
 */
@Slf4j
@SuppressWarnings({"unchecked", "JpaQlInspection"})
public class HibernateUtilsTest extends TestCase {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetSessionFactory(){
        SessionFactory sessionFactory = HibernateUtils.getSessionFactory();
        assertNotNull(sessionFactory);
    }

    @Test
    public void testQuery(){
        SessionFactory sessionFactory = HibernateUtils.getSessionFactory();

        Session session  = sessionFactory.openSession();

        List<Account> account = (List<Account>)session.createCriteria(Account.class).list();

        session.close();

        assertTrue(account.size() > 0);
    }
}