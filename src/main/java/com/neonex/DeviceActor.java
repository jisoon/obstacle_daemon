package com.neonex;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.neonex.message.StartMsg;
import com.neonex.model.CompModelEvent;
import com.neonex.model.EqStatus;
import com.neonex.utils.HibernateUtils;
import com.neonex.watchers.CpuWatcher;
import com.neonex.watchers.DisconnectWatcher;
import com.neonex.watchers.MemWatcher;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : 지순
 * @packageName : com.neonex
 * @since : 2016-02-16
 */
@Slf4j
@SuppressWarnings("JpaQlInspection")
public class DeviceActor extends UntypedActor {
    private ActorRef cpuWatcher;
    private ActorRef memWatcher;
    private ActorRef disconnectWatcher;

    public DeviceActor() {
        cpuWatcher = context().actorOf(Props.create(CpuWatcher.class), "cpuWatcher");
        memWatcher = context().actorOf(Props.create(MemWatcher.class), "memWatcher");
        disconnectWatcher = context().actorOf(Props.create(DisconnectWatcher.class), "disconnectWatcher");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartMsg) {
            log.info("=== daemon message received!!! ===");
            List<EqStatus> devices = fetchDevice();

            detectDisconnect(devices);

            Collection<String> connectionEqIds = convertEqIdCollection(filterConnectionDevice(devices));
            Collection<String> disconnectionEqIds = convertEqIdCollection(filterDisconnectionDevice(devices));

            cpuWatcher.tell(new StartMsg(connectionEqIds), getSelf());
            memWatcher.tell(new StartMsg(connectionEqIds), getSelf());
            disconnectWatcher.tell(new StartMsg(disconnectionEqIds), getSelf());
        } else {
            unhandled(message);
        }
    }

    private Collection<EqStatus> filterConnectionDevice(List<EqStatus> devices) {
        return Collections2.filter(
                devices, new Predicate<EqStatus>() {
                    @Override
                    public boolean apply(EqStatus devices) {
                        return Objects.equal(devices.getConnectYn(), "Y");
                    }
                });
    }

    private Collection<EqStatus> filterDisconnectionDevice(List<EqStatus> devices) {
        return Collections2.filter(
                devices, new Predicate<EqStatus>() {
                    @Override
                    public boolean apply(EqStatus devices) {
                        return Objects.equal(devices.getConnectYn(), "N");
                    }
                });
    }

    /**
     * 현재 연결된 장비들의 상태 정보 조회
     *
     * @return
     */
    public List<EqStatus> fetchDevice() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        List<EqStatus> eqStatus = session.createCriteria(EqStatus.class)
                .add(Restrictions.eq("connectYn", "Y"))
                .list();
        session.close();
        return eqStatus;
    }

    /**
     * 미연결 장애 감지
     *
     * @param devices
     * @return
     */
    public void detectDisconnect(List<EqStatus> devices) {


        // 미연결 임계치 조회
        // 장비의 모델 코드로 미연결 임계치 정보를 조회 할 수 있음
        Map<String, Object> thresHold = fetchConnetionThresHold();

        Session session = HibernateUtils.getSessionFactory().openSession();
        session.getTransaction().begin();
        try {
            for (EqStatus device : devices) {
                String deviceModelCode = device.getEqInfo().getEqModel().getModelCode();

                // 장비의 미연결 임계치가 존재 하면 미연결 여부를 체크 하고
                // 존재 하지 않는다면 skip 함
                if (thresHold.containsKey(deviceModelCode)) {

                    // 장비 모델 코드로 미연결 인터벌 값을 조회
                    int connectionInterval = (Integer) thresHold.get(device.getEqInfo().getEqModel().getModelCode());

                    long deviceLastConnTime = 0L;
                    if (Strings.isNullOrEmpty(device.getLastCommTime())) {
                        deviceLastConnTime = Long.parseLong(device.getLastCommTime());
                    }
                    long thresholdLastCommTime = calcConnectionThresHoldTime(connectionInterval);


                    // DB 시간과 임계치 시간 비교
                    if (deviceLastConnTime < thresholdLastCommTime) {
                        // device list 상태를 N 으로 변경
                        device.setConnectYn("N");
                    }
                }
            }
        } catch (NumberFormatException ne) {
            session.getTransaction().rollback();
        } catch (Exception e) {
            log.info("Exception ");
            e.printStackTrace();
        }
        session.getTransaction().commit();
        session.close();
    }

    /**
     * 연결 임계치 조회
     * 장비의 모델 코드로 연결 임계치를 조회 할 수 있도록 Map 으로 구성
     *
     * @return map
     */
    public Map<String, Object> fetchConnetionThresHold() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        List<CompModelEvent> thresholdList = session.createCriteria(CompModelEvent.class)
                .add(Restrictions.eq("eventCode", "CON0002"))
                .list();
        Map<String, Object> threshold = new HashMap<String, Object>();
        for (CompModelEvent event : thresholdList) {
            threshold.put(event.getModelCode(), event.getMaxValue());
        }
        session.close();
        return threshold;

    }

    /**
     * 장비 상태 조회
     * @param eqId
     * @return
     */
    public EqStatus findDevice(String eqId) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        EqStatus device = session.get(EqStatus.class, eqId);
        session.close();
        return device;
    }

    private Long calcConnectionThresHoldTime(int thresHoldTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, (int) ((-1) * thresHoldTime));
        return Long.parseLong(formatter.format(cal.getTime()));
    }

    private Collection<String> convertEqIdCollection(Collection<EqStatus> eqStatusList) {
        return Collections2.transform(eqStatusList, new Function<EqStatus, String>() {
            @Override
            public String apply(EqStatus eqStatus) {
                return eqStatus.getEqId();
            }
        });
    }
}
