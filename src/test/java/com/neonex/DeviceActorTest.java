package com.neonex;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.neonex.message.StartMsg;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@Slf4j
public class DeviceActorTest extends TestCase {

    public static final String CRITICAL = "CRITICAL";
    private ActorSystem system;
    private Props props;

    private Session session;
    private SessionFactory sessionFactory;

    private DeviceActor deviceActor;
    private TestActorRef<DeviceActor> testActorRef;


    @Before
    public void setUp() throws Exception {
        sessionFactory = HibernateUtils.getSessionFactory();
        session = sessionFactory.openSession();

        system = ActorSystem.create();
        props = Props.create(DeviceActor.class);

        testActorRef = TestActorRef.create(system, props, "DeviceActorTest");
        deviceActor = testActorRef.underlyingActor();


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
        final Props props = Props.create(DeviceActor.class);
        final TestActorRef<DeviceActor> testRef = TestActorRef.create(system, props, "testDeviceStatus");
        testRef.tell(new StartMsg(), ActorRef.noSender());

        // then
        EqStatus device = deviceActor.findDevice(testDevice.getEqId());
        assertThat(device.getConnectYn()).isEqualTo("N");

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
                log.info("eq modelCode {}", device.getEqInfo().getEqId());
                log.info("eq modelCode {}", device.getEqInfo().getEqModel().getModelCode());
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
        deviceActor.detectDisconnect(devices);
        testDevice = session.get(EqStatus.class, "1");

        // then

        EqStatus disConnectedDevice = deviceActor.findDevice(testDevice.getEqId());
        assertThat(disConnectedDevice.getConnectYn()).isEqualTo("N");
    }



    @Test
    public void testCompModelThresHold() throws Exception {
        // given


        // when
        Map<String, Object> thresHold = deviceActor.fetchConnetionThresHold();

        // then
        assertThat(thresHold.containsKey("MXR-410K")).isTrue();

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