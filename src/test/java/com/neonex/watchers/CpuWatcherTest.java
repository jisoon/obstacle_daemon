package com.neonex.watchers;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqCpu;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-23
 */
@Slf4j
public class CpuWatcherTest {

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

        // when
        List<EqCpu> eqCpuStats = cpuWatcher.fetchEqCpuStatus(eqStats);

        // then
        log.info("{}", eqCpuStats);
        assertThat(eqCpuStats).isNotEmpty();
        assertThat(eqCpuStats).hasSize(1);
        assertThat(eqCpuStats.get(0).getCpuUsage()).isNotZero();
    }


    /**
     * 현재 CPU 상태로 임계치를 초과지 확인
     *
     * @throws Exception
     */
    @Test
    public void testDetectCpuObstacle() throws Exception {
        // given
        EqCpu eqCpu = new EqCpu();
        Double cpuMaxThresHold = Double.valueOf(90);
        Double cpuMinThresHold = Double.valueOf(10);

        boolean isOccurEvent = false;

        eqCpu.setCpuUsage(90);
        isOccurEvent = cpuWatcher.detectCpuObstacle(eqCpu.getCpuUsage(), cpuMinThresHold, cpuMaxThresHold);
        assertThat(isOccurEvent).isFalse();

        eqCpu.setCpuUsage(91);
        isOccurEvent = cpuWatcher.detectCpuObstacle(eqCpu.getCpuUsage(), cpuMinThresHold, cpuMaxThresHold);
        assertThat(isOccurEvent).isFalse();

        eqCpu.setCpuUsage(89);
        isOccurEvent = cpuWatcher.detectCpuObstacle(eqCpu.getCpuUsage(), cpuMinThresHold, cpuMaxThresHold);
        assertThat(isOccurEvent).isTrue();

        eqCpu.setCpuUsage(9);
        isOccurEvent = cpuWatcher.detectCpuObstacle(eqCpu.getCpuUsage(), cpuMinThresHold, cpuMaxThresHold);
        assertThat(isOccurEvent).isFalse();

        eqCpu.setCpuUsage(10);
        isOccurEvent = cpuWatcher.detectCpuObstacle(eqCpu.getCpuUsage(), cpuMinThresHold, cpuMaxThresHold);
        assertThat(isOccurEvent).isTrue();

        eqCpu.setCpuUsage(11);
        isOccurEvent = cpuWatcher.detectCpuObstacle(eqCpu.getCpuUsage(), cpuMinThresHold, cpuMaxThresHold);
        assertThat(isOccurEvent).isTrue();

    }

    @Test
    public void testInsertCpuObstacle() throws Exception {
        // given
        EqCpu eqCpu = new EqCpu();
        eqCpu.setEqId("1");

        // when
        boolean inserted = cpuWatcher.insertEvent(eqCpu);

        // then
        assertThat(inserted).isTrue();

    }
}