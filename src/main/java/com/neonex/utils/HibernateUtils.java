package com.neonex.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author : 지순
 * @packageName : com.neonex.utils
 * @since : 2016-02-17
 */
public class HibernateUtils {

    static SessionFactory sessionFactory;

    static {
        System.out.println(">>>>>>>>>>> SessionFactory InInitializer");
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (java.lang.ExceptionInInitializerError e) {
            e.printStackTrace();
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
