package com.neonex;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.neonex.dto.EqStatus;
import com.neonex.message.StartMsg;
import com.neonex.utils.HibernateUtils;
import junit.framework.TestCase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */

public class DeviceActorTest extends TestCase {

    private final static Logger logger = LoggerFactory.getLogger(DeviceActorTest.class);

    private ActorSystem system;
    private Props props;
    private DeviceActor deviceActor;
    private TestActorRef<DeviceActor> testDeviceActor;
    private Session session;
    private SessionFactory sessionFactory;


    @Before
    public void setUp() throws Exception {
        sessionFactory = HibernateUtils.getSessionFactory();
        system = ActorSystem.create();
        props = Props.create(DeviceActor.class, HibernateUtils.getSessionFactory());
        testDeviceActor = TestActorRef.create(system, props, "DeviceActorTest");
        deviceActor = testDeviceActor.underlyingActor();
        session = sessionFactory.openSession();
    }

    @After
    public void tearDown() throws Exception {
        JavaTestKit.shutdownActorSystem(system);
        session.close();
        system = null;
    }

    @Test
    public void testOnReceive() throws Exception {
        // given
        EqStatus testDevice = initTestDeviceDisconnected();

        // when
        // akka actor 를 이용해서 로직 실행
        final Props props = Props.create(DeviceActor.class, sessionFactory);
        final TestActorRef<DeviceActor> testRef = TestActorRef.create(system, props, "testDeviceStatus");
        testRef.tell(new StartMsg(), ActorRef.noSender());

        // then
        EqStatus device = deviceActor.findDevice(testDevice.getEqId());
        assertThat(device.getConnectYn(), is("N"));

    }

    @Test
    public void testFetchDeviceStatus() throws Exception {

        //given
        List<EqStatus> status = deviceActor.fetchDevice();

        //then
        for (EqStatus device : status) {
            if (device.getConnectYn().equals("N")) {
                fail();
            }
            if (device.getEqInfo().getEqModel().getModelCode() == null) {
                fail();
            } else {
                logger.info("eq modelCode {}", device.getEqInfo().getEqId());
                logger.info("eq modelCode {}", device.getEqInfo().getEqModel().getModelCode());
            }


        }
    }

    @Test
    public void testDetectDisconnect() throws Exception {
        // given
        // 테스트 장비의 마지막 연결 시간을 임계치 보다 이전의 시간으로 세팅
        EqStatus testDevice = initTestDeviceDisconnected();

        List<EqStatus> devices = deviceActor.fetchDevice();

        // when
        int disconnectCount = deviceActor.detectDisconnect(devices);
        testDevice = session.get(EqStatus.class, "1");

        // then
        assertThat(disconnectCount, is(not(0)));
        EqStatus disConnectedDevice = deviceActor.findDevice(testDevice.getEqId());
        assertThat(disConnectedDevice.getConnectYn(), is("N"));
    }

    @Test
    public void testUpdateStatusDisconnect() throws Exception {
        // given
        EqStatus testDevice = initTestDeviceDisconnected();

        // when
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        deviceActor.updateStatusDisconnect(session, testDevice);
        session.getTransaction().commit();
        session.close();

        // then
        EqStatus eqStatus = deviceActor.findDevice(testDevice.getEqId());
        assertThat(eqStatus.getConnectYn(), is("N"));

    }

    @Test
    public void testInsertDisconnectEvent() throws Exception {
        // given
        EqStatus testDevice = getTestDeviceInfo();

        // when
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.getTransaction().begin();
        boolean isInserted = deviceActor.insertDisconnectEvent(session, testDevice.getEqId());
        if (!isInserted) {
            session.getTransaction().rollback();
            session.close();
            fail();
        }else{
            session.getTransaction().commit();
            session.close();
            assertTrue(true);
        }
    }

    @Test
    public void testInitObstalceEvent() throws Exception {
        // given
        EqStatus eqStatus = getTestDeviceInfo();

        // when
        boolean isInit = deviceActor.initObstalceEvent(eqStatus.getEqId());

        // then
        assertThat(isInit, is(true));


    }

    @Test
    public void testCompModelThresHold() throws Exception {
        // given


        // when
        Map<String, Object> thresHold = deviceActor.fetchConnetionThresHold();

        // then
        assertThat(thresHold.containsKey("MXR-410K"), is(true));

    }

    @Test
    public void testHasDisConnectionEvent() throws Exception {

        EqStatus eqStatus = getTestDeviceInfo();

        // given
        deviceActor.insertDisconnectEvent(session, eqStatus.getEqId());

        // when
        boolean hasNoDisconnectionEvent = deviceActor.hasNoDisconnectionEvent(initTestDeviceDisconnected().getEqId());

        // then
        assertThat(hasNoDisconnectionEvent, is(false));

    }

    private EqStatus initTestDeviceDisconnected() {
        session.getTransaction().begin();
        EqStatus testDevice = getTestDeviceInfo();
        session.update(testDevice);
        session.getTransaction().commit();
        return testDevice;
    }

    private EqStatus getTestDeviceInfo() {
        EqStatus testDevice = new EqStatus();
        testDevice.setEqId("1");
        testDevice.setLastCommTime("20000217164226");
        testDevice.setConnectYn("Y");
        return testDevice;
    }
}