package com.neonex.detector;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.neonex.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-23
 */
@Slf4j
public class CpuEventDetectorTest {

    public static final String CPU_EVENT_CODE = "RES0001";

    private CpuEventDetector cpuEventDetector;
    private TestActorRef<CpuEventDetector> testActorRef;
    private String testEqId;

    private ActorSystem system;
    private Props props;

    private Collection<String> eqIds;


    @Before
    public void setUp() throws Exception {

        testEqId = "1";
        system = ActorSystem.create();
        props = Props.create(CpuEventDetector.class);

        testActorRef = TestActorRef.create(system, props, "CpuEventDetector");
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
    public void testDetect() throws Exception {
        // when
        List<Event> events = cpuEventDetector.detect(eqIds);

        // then
        assertThat(events).isNotEmpty();

    }

    @Test
    public void testInsertCpuEvent() throws Exception {
        // given
        List<Event> events = new ArrayList<Event>();
        events.add(createTestCpuErrorEvent());

        // when
        boolean isSaved = cpuEventDetector.save(events);

        // then
        assertThat(isSaved).isTrue();

    }

    @Test
    public void testIsCpuObstacle() throws Exception {
        // given
        Event testEvent = createTestCpuErrorEvent();

        // when
        boolean isCpuObstacle = cpuEventDetector.isObstacleOccurrence(testEvent);

        // then
        assertThat(isCpuObstacle).isTrue();

    }


    private Event createTestCpuErrorEvent() {
        Event event = new Event();
        event.setEqId(testEqId);
        event.setEventCode(CPU_EVENT_CODE);
        event.setEventCont("CPU 사용률 " + "90 " + "%");
        event.setEventLevelCode("ERROR");
        event.setProcessYn("N");
        event.setOccurDate("20160317125959");
        return event;
    }

}