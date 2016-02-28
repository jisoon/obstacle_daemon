package com.neonex.watchers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.neonex.message.StartMsg;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqCpu;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;


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
    private Collection<String> eqIds;
    private final String CRITICAL = "CRITICAL";


    @Before
    public void setUp() throws Exception {


        system = ActorSystem.create();
        props = Props.create(CpuWatcher.class);

        testActorRef = TestActorRef.create(system, props, "DeviceActorTest");
        cpuWatcher = testActorRef.underlyingActor();
        eqIds = new ArrayList<String>();
        eqIds.add("1");

    }

    @After
    public void tearDown() throws Exception {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testOnReceive() throws Exception {
        EqCpu eqCpu = new EqCpu();
        eqCpu.setEqId("1");
        eqCpu.setCpuMaker("INTEL");
        eqCpu.setCpuModel("I5-3230M");
        eqCpu.setCoreNum("1");
        eqCpu.setCpuUsage(90);

        sessionFactory = HibernateUtils.getSessionFactory();
        session = sessionFactory.openSession();

        session.getTransaction().begin();
        session.update(eqCpu);
        session.getTransaction().commit();

        session.close();


        try {
            final Props props = Props.create(CpuWatcher.class);
            final TestActorRef<CpuWatcher> testRef = TestActorRef.create(system, props, "testCpuWatcher");
            StartMsg startMsg = new StartMsg(eqIds);
            testRef.tell(startMsg, ActorRef.noSender());
        } catch (Exception e) {
            e.printStackTrace();
            fail("onReceive fail");
        }
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
    public void testFetchEqCpuStatus() throws Exception {
        // given
        EqStatus eqStatus = new EqStatus();
        eqStatus.setEqId("1");

        Collection<String> eqIdList = new ArrayList<String>();
        eqIdList.add("1");

        // when
        List<EqCpu> eqCpuStats = cpuWatcher.fetchEqCpuStatus(eqIdList);

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
        int cpuMaxThresHold = 90;
        int cpuMinThresHold = 10;

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
        boolean inserted = cpuWatcher.insertEvent("1", Double.valueOf(80), CRITICAL);

        // then
        assertThat(inserted).isTrue();

    }

    @Test
    public void testFindEqModelCode() throws Exception {
        // given

        // when
        String eqModelCode = cpuWatcher.findEqModelCode("1");

        // then
        log.info(eqModelCode);
        assertThat(eqModelCode).isNotNull();
        assertThat(eqModelCode).isNotEmpty();
    }

    @Test
    public void testHasCpuEventInDb() {
        // given
        String testEqId = "1";
        cpuWatcher.insertEvent(testEqId, new Double(90), "INFO");

        // when
        boolean hasCpuEvent = cpuWatcher.hasCpuEvent(testEqId);

        //then
        assertThat(hasCpuEvent).isTrue();
    }
}