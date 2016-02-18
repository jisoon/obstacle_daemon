package com.neonex;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.neonex.dto.DeviceStatus;
import com.neonex.message.StartMsg;
import com.neonex.utils.HibernateUtils;
import junit.framework.TestCase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */

public class DeviceActorTest extends TestCase {

    ActorSystem system;
    Props props;
    DeviceActor deviceActor;
    TestActorRef<DeviceActor> ref;
    Session session;
    SessionFactory sessionFactory;


    @Before
    public void setUp() throws Exception {
        sessionFactory = HibernateUtils.getSessionFactory();
        system = ActorSystem.create();
        props = Props.create(DeviceActor.class, HibernateUtils.getSessionFactory());
        ref = TestActorRef.create(system, props, "DeviceActorTest");
        deviceActor = ref.underlyingActor();
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
        DeviceStatus testDevice = initTestDeviceDisconnected();

        // when
        final Props props = Props.create(DeviceActor.class);
        final TestActorRef<DeviceActor> ref = TestActorRef.create(system, props, "testDeviceStatus");
        ref.tell(new StartMsg(), ActorRef.noSender());

        // then
        DeviceStatus device = deviceActor.findDevice(testDevice.getEqId());
        assertThat(device.getConnectYn(), is("N"));

    }


    @Test
    public void testFetchDeviceStatus() throws Exception {

        //given
        List<DeviceStatus> status = deviceActor.fetchDevice();

        //then
        for (DeviceStatus device : status) {
            if (device.getConnectYn().equals("N")) {
                fail();
            }
        }
    }

    @Test
    public void testDetectDisconnect() throws Exception {

        // given
        // 테스트 장비의 마지막 연결 시간을 임계치 보다 이전의 시간으로 세팅
        DeviceStatus testDevice = initTestDeviceDisconnected();

        List<DeviceStatus> devices = deviceActor.fetchDevice();

        // when
        int disconnectCount = deviceActor.detectDisconnect(devices);
        testDevice = session.get(DeviceStatus.class, "1");

        // then
        assertThat(disconnectCount, is(not(0)));
        DeviceStatus disConnectedDevice = deviceActor.findDevice(testDevice.getEqId());
        assertThat(disConnectedDevice.getConnectYn(), is("N"));


    }

    private DeviceStatus initTestDeviceDisconnected() {
        session.getTransaction().begin();
        DeviceStatus testDevice = new DeviceStatus();
        testDevice.setEqId("1");
        testDevice.setLastCommTime("20000217164226");
        testDevice.setConnectYn("Y");
        session.update(testDevice);
        session.getTransaction().commit();
        return testDevice;
    }

}