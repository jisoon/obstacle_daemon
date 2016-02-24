package com.neonex.watchers;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.neonex.DeviceActorTest;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqCpu;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.fest.assertions.api.Assertions.assertThat;


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
        assertThat(modelEventList).isNotEmpty();

    }

    @Test
    public void testfetchEqCpuStatus() throws Exception {


        // given

        EqStatus eqStatus = new EqStatus();
        eqStatus.setEqId("1");

        List<EqStatus> eqStats = new ArrayList<EqStatus>();
        eqStats.add(eqStatus);

        cpuWatcher.setEqStatusList(eqStats);

        // when
        List<EqCpu> eqCpuStats = cpuWatcher.fetchEqCpuStatus();

        // then
        logger.info("{}", eqCpuStats);
        assertThat(eqCpuStats).isNotEmpty();
        assertThat(eqCpuStats.get(0).getEqId()).isNotEmpty();
        assertThat(eqCpuStats.get(0).getEqId()).isNotNull();
        assertThat(eqCpuStats.get(0).getCpuUsage()).isNotZero();
    }


    @Test
    public void testDetectCpuObstacle() throws Exception {

        fail();

        // given

        // when

        // then

    }
}