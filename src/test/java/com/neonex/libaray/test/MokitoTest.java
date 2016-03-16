package com.neonex.libaray.test;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author : 지순
 * @packageName : com.neonex.libaray.test
 * @since : 2016-03-15
 */
@Slf4j
public class MokitoTest extends TestCase {

    @Test
    public void testWhenThen() throws Exception {
        // given
        Map<String, String> testMock = mock(Map.class);
        when(testMock.get("test1")).thenReturn("test1");
        // when
        String mockString = testMock.get("test1");

        // then
        assertThat(mockString).isEqualTo("test1");

    }
}
