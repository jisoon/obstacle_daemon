package com.neonex.watchers;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author : 지순
 * @packageName : com.neonex.watchers
 * @since : 2016-02-25
 */
@Slf4j
public class DisconnectWatcherTest {

    private DisconnectWatcher disconnectWatcher;
    private TestActorRef<DisconnectWatcher> testDisconnectWatcher;

    private ActorSystem system;
    private Props props;

    private Collection<String> eqIds;
    private final String CRITICAL = "CRITICAL";

    @Before
    public void setUp() throws Exception {


        system = ActorSystem.create();
        props = Props.create(DisconnectWatcher.class);

        testDisconnectWatcher = TestActorRef.create(system, props, "TestDisconnectWatcher");
        disconnectWatcher = testDisconnectWatcher.underlyingActor();
        eqIds = new ArrayList<String>();
        eqIds.add("1");
        eqIds.add("2");

    }

    @After
    public void tearDown() throws Exception {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }


    @Test
    public void testUpdateStatusDisconnect() throws Exception {


        // when
        disconnectWatcher.updateStatusDisconnect(eqIds);

        // then

    }

    @Test
    public void testInsertDisconnectEvent() throws Exception {


        // when
        disconnectWatcher.insertDisconnectEvent(eqIds);

    }

    @Test
    public void testInitObstalceEvent() throws Exception {

        // when
        disconnectWatcher.initObstalceEvent(eqIds);

        // then


    }

    @Test
    public void testHasDisConnectionEvent() throws Exception {

        // given
        disconnectWatcher.insertDisconnectEvent(eqIds);

        // when
        boolean hasNoDisconnectionEvent = disconnectWatcher.hasNoDisconnectionEvent("1");

        // then
        assertThat(hasNoDisconnectionEvent).isFalse();

    }

    private EqStatus initTestDeviceDisconnected() {
        Session session = HibernateUtils.getSessionFactory().openSession();
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