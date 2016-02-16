package com.neonex;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */

public class DeviceStatusTest extends TestCase {

    static ActorSystem system;


    @BeforeClass
    public void setUp() throws Exception {
        system = ActorSystem.create();
    }

    @AfterClass
    public void tearDown() throws Exception {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testFetchDeviceStatus() throws Exception {


        final Props props = Props.create(DeviceStatus.class);
        final TestActorRef<DeviceStatus> ref = TestActorRef.create(system, props, "testA");
        final DeviceStatus actor = ref.underlyingActor();
        assertTrue(actor.fetchDeviceStatus());
    }

//    JavaTestKit 으로 테스트
//    @Test
//    public void testJavaTestkit() throws Exception {
//        /*
//     * Wrap the whole test procedure within a testkit constructor
//     * if you want to receive actor replies or use Within(), etc.
//     */
//        new JavaTestKit(system) {
//            {
//                final Props props = Props.create(DeviceStatus.class);
//                final ActorRef subject = system.actorOf(props);
//
//                // can also use JavaTestKit “from the outside”
//                final JavaTestKit probe = new JavaTestKit(system);
//                // “inject” the probe by passing it to the test subject
//                // like a real resource would be passed in production
//                subject.tell(probe.getRef(), getRef());
//                // await the correct response
//                expectMsgEquals(duration("1 second"), "done");
//
//                // the run() method needs to finish within 3 seconds
//                new Within(duration("3 seconds")) {
//                    protected void run() {
//
//                        subject.tell("hello", getRef());
//
//                        // This is a demo: would normally use expectMsgEquals().
//                        // Wait time is bounded by 3-second deadline above.
//                        new AwaitCond() {
//                            protected boolean cond() {
//                                return probe.msgAvailable();
//                            }
//                        };
//
//                        // response must have been enqueued to us before probe
//                        expectMsgEquals(Duration.Zero(), "world");
//                        // check that the probe we injected earlier got the msg
//                        probe.expectMsgEquals(Duration.Zero(), "hello");
//                        Assert.assertEquals(getRef(), probe.getLastSender());
//
//                        // Will wait for the rest of the 3 seconds
//                        expectNoMsg();
//                    }
//                };
//            }
//        };
//    }

}