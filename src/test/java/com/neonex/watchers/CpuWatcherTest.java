package com.neonex.watchers;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.neonex.DeviceActorTest;
import com.neonex.dto.CompModelEvent;
import com.neonex.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-23
 */
public class CpuWatcherTest {

    private final static Logger logger = LoggerFactory.getLogger(DeviceActorTest.class);

    private CpuWatcher cpuWatcher;
    private TestActorRef<CpuWatcher> testActorRef;

    private ActorSystem system;
    private Props props;

    private Session session;
    private SessionFactory sessionFactory;


    @Before
    public void setUp() throws Exception {
        sessionFactory = HibernateUtils.getSessionFactory();
        session = sessionFactory.openSession();

        system = ActorSystem.create();
        props = Props.create(CpuWatcher.class);

        testActorRef = TestActorRef.create(system, props, "DeviceActorTest");
        cpuWatcher = testActorRef.underlyingActor();

    }

    @Test
    public void testOnReceive() throws Exception {

    }

    @Test
    public void testCpuEventThresHold() throws Exception {
        // given

        // when
        List<CompModelEvent> modelEventList = cpuWatcher.fetchCpuThresHold();

        // then
        assertThat(modelEventList.size(), is(not(0)));

    }

    @Test
    public void testDetectCpuObstacle() throws Exception {

        fail();

        // given

        // when

        // then

    }
}