package com.neonex.detector;

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
public class CpuEventDetectorTest {

    private CpuEventDetector cpuEventDetector;
    private TestActorRef<CpuEventDetector> testActorRef;
    private String testEqId;

    private ActorSystem system;
    private Props props;

    private Session session;
    private SessionFactory sessionFactory;
    private Collection<String> eqIds;
    private final String CRITICAL = "CRITICAL";


    @Before
    public void setUp() throws Exception {

        testEqId = "1";
        system = ActorSystem.create();
        props = Props.create(CpuEventDetector.class);

        testActorRef = TestActorRef.create(system, props, "DeviceActorTest");
        cpuEventDetector = testActorRef.underlyingActor();
        eqIds = new ArrayList<String>();
        eqIds.add(testEqId);


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
        eqCpu.setCpuUsage(50);

        sessionFactory = HibernateUtils.getSessionFactory();
        session = sessionFactory.openSession();
        session.getTransaction().begin();
        session.update(eqCpu);
        session.getTransaction().commit();
        session.close();


        try {
            final Props props = Props.create(CpuEventDetector.class);
            final TestActorRef<CpuEventDetector> testRef = TestActorRef.create(system, props, "testCpuWatcher");
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
        List<CompModelEvent> modelEventList = cpuEventDetector.fetchCpuThresHold();

        // then
        assertThat(modelEventList).isNotEmpty();

    }

    @Test
    public void thresholdMinMax() throws Exception {
        // given

        // when
        List<CompModelEvent> modelEventList = cpuEventDetector.fetchCpuThresHold();

        // then

    }

    @Test
    public void findEqCpuStatusByEqid() throws Exception {
        // given
        EqStatus eqStatus = new EqStatus();
        eqStatus.setEqId("1");

        Collection<String> eqIdList = new ArrayList<String>();
        eqIdList.add("1");

        // when
        List<EqCpu> eqCpuStats = cpuEventDetector.findCpuStatusBy(eqIdList);

        // then
        assertThat(eqCpuStats).hasSize(1);
    }

    /**
     * 현재 CPU 상태로 임계치를 초과지 확인
     *
     * @throws Exception
     */
    @Test
    public void testDetectCpuObstacle() throws Exception {
        // given
        CompModelEvent threshold = new CompModelEvent();
        threshold.setMaxValue(90);
        threshold.setMinValue(10);

        boolean isOccurEvent;

        isOccurEvent = cpuEventDetector.isCpuUsageObstacle(90D, threshold);
        assertThat(isOccurEvent).isFalse();

        isOccurEvent = cpuEventDetector.isCpuUsageObstacle(91D, threshold);
        assertThat(isOccurEvent).isFalse();

        isOccurEvent = cpuEventDetector.isCpuUsageObstacle(89D, threshold);
        assertThat(isOccurEvent).isTrue();

        isOccurEvent = cpuEventDetector.isCpuUsageObstacle(9D, threshold);
        assertThat(isOccurEvent).isFalse();

        isOccurEvent = cpuEventDetector.isCpuUsageObstacle(10D, threshold);
        assertThat(isOccurEvent).isTrue();

        isOccurEvent = cpuEventDetector.isCpuUsageObstacle(11D, threshold);
        assertThat(isOccurEvent).isTrue();

    }

    @Test
    public void testInsertCpuObstacle() throws Exception {
        // given
        EqCpu eqCpu = new EqCpu();
        eqCpu.setEqId("1");

        // when
        boolean inserted = cpuEventDetector.insertCpuEvent("1", Double.valueOf(80), CRITICAL);

        // then
        assertThat(inserted).isTrue();

    }

    @Test
    public void testFindEqModelCode() throws Exception {
        // given

        // when
        String eqModelCode = cpuEventDetector.findEqModelCode("1");

        // then
        assertThat(eqModelCode).isNotEmpty();
    }

    @Test
    public void testNoHasEqualsCpuEventLevel() {
        // given
        cpuEventDetector.insertCpuEvent(testEqId, new Double(90), "INFO");

        // when
        boolean hasCpuEvent = cpuEventDetector.noHasEqualsCpuEventLevel(testEqId, "INFO");

        //then
        assertThat(hasCpuEvent).isFalse();
    }

    @Test
    public void testNoHasEqualsCpuEventLevelInDbEmpty() throws Exception {
        // given
        cpuEventDetector.initCpuEventByMsgUpdateEventLevel(testEqId);

        // when
        boolean noHasCpuEvent = cpuEventDetector.noHasEqualsCpuEventLevel(testEqId, "INFO");

        //then
        assertThat(noHasCpuEvent).isTrue();

    }


    @Test
    public void testinitCpuEventByMsgUpdateEventLevel() {

        //when
        cpuEventDetector.initCpuEventByMsgUpdateEventLevel(testEqId);

        //then
        boolean hasCpuEventCode = cpuEventDetector.noHasEqualsCpuEventLevel(testEqId, "INFO");
        assertThat(hasCpuEventCode).isFalse();

    }

}