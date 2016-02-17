package com.neonex;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.neonex.dto.DeviceStatus;
import com.neonex.utils.HibernateUtils;
import junit.framework.TestCase;
import org.hibernate.Session;
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


    @Before
    public void setUp() throws Exception {
        system = ActorSystem.create();
        props = Props.create(DeviceActor.class);
        ref = TestActorRef.create(system, props, "DeviceActorTest");
        deviceActor = ref.underlyingActor();
        session = HibernateUtils.getSessionFactory().openSession();
    }

    @After
    public void tearDown() throws Exception {
        JavaTestKit.shutdownActorSystem(system);
        session.close();
        system = null;
    }

    @Test
    public void testFetchDeviceStatus() throws Exception {

        //given
        List<DeviceStatus> status = deviceActor.fetchDevice(session);

        //then
        for (DeviceStatus device : status) {
            if(device.getConnectYn().equals("N")){
                fail();
            }
        }
    }
    
    @Test
    public void testDetectDisconnect() throws Exception {

        session.getTransaction().begin();
        // given
        DeviceStatus disConnectDevice = new DeviceStatus();
        disConnectDevice.setEqId("1");
        disConnectDevice.setLastCommTime("20000217164226");
        disConnectDevice.setConnectYn("Y");
        session.update(disConnectDevice);

        try {
            List<DeviceStatus> devices = deviceActor.fetchDevice(session);

            // when
            int disconnectCount = deviceActor.detectDisconnect(session, devices);
            disConnectDevice = session.get(DeviceStatus.class, "1");

            // then
            assertThat(disconnectCount, is(not(0)));
            assertThat(disConnectDevice.getConnectYn(), is("N"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        session.getTransaction().rollback();
    }

}