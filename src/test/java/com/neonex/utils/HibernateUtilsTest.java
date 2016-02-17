package com.neonex.utils;

import junit.framework.TestCase;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author : 지순
 * @packageName : com.neonex.utils
 * @since : 2016-02-17
 */
public class HibernateUtilsTest extends TestCase {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetSessionFactory(){
        SessionFactory sessionFactory = HibernateUtils.getSessionFactory();

        assertNotNull(sessionFactory);

    }
}