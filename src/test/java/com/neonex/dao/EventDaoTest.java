package com.neonex.dao;

import com.neonex.model.Event;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author : 지순
 * @packageName : com.neonex.appender
 * @since : 2016-03-16
 */
@Slf4j
public class EventDaoTest extends TestCase {


    private final String CPU_EVENT_CODE = "RES0001";


    EventDao eventDao;

    @Before
    public void setUp() {
        eventDao = new EventDao();
    }


    @Test
    public void testAppendEvent() throws Exception {
        // given
        Event event = new Event();
        event.setEqId("1");
        event.setEventLevelCode("INFO");
        event.setEventCont("CPU 이벤트.");
        event.setEventCode(CPU_EVENT_CODE);

        // when
        boolean isSave = eventDao.save(event);

        // then
        assertThat(isSave).isTrue();

    }

}
