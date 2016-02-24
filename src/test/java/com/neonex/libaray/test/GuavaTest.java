package com.neonex.libaray.test;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.fest.util.Strings;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author : 지순
 * @packageName : com.neonex.libaray.test
 * @since : 2016-02-24
 */
@Slf4j
public class GuavaTest extends TestCase {

    List<State> states;


    @Before
    public void setUp() {
        states = Lists.newArrayList();

        states.add(new State("WI", "Wisconsin", "MDW", 5726398));
        states.add(new State("FL", "Florida", "SE", 19317568));
        states.add(new State("IA", "Iowa", "MDW", 3078186));
        states.add(new State("CA", "California", "W", 38041430));
        states.add(new State("NY", "New York", "NE", 19570261));
        states.add(new State("CO", "Colorado", "W", 5187582));
        states.add(new State("OH", "Ohio", "MDW", 11544225));
        states.add(new State("ME", "Maine", "NE", 1329192));
        states.add(new State("SD", "South Dakota", "MDW", 833354));
        states.add(new State("TN", "Tennessee", "SE", 6456243));
        states.add(new State("OR", "Oregon", "W", 3899353));
    }

    @Test
    public void testObjectNullCheck() throws Exception {
        // given
        String test = null;
        // when
        boolean isNull = Objects.equal(test, "test");

        // then
        assertThat(isNull).isFalse();

    }

    @Test
    public void testStringNullCheck() throws Exception {
        // given
        String test = null;

        // when
        boolean isNull = Strings.isNullOrEmpty(test);


        // then
        assertThat(isNull).isTrue();

    }

    @Test
    public void testStringEqTest() throws Exception {
        // given
        String test = "tes";

        // when
        boolean isEquals = Objects.equal(test, "tes");

        // then
        assertThat(isEquals).isTrue();

    }

    @Test
    public void testFilter() {

        Collection<State> filterStrings = Collections2.filter(
                states, new Predicate<State>() {
                    @Override
                    public boolean apply(State state) {
                        return Objects.equal(state.getName(), "Iowa");
                    }
                });

        log.info("{}", filterStrings);

        assertThat(filterStrings).hasSize(1);
    }


    class State {
        private String stateCode;
        private String name;
        private String regionCode;
        private int population;

        public State() {

        }

        public State(String stateCode, String name, String regionCode, int population) {
            this.stateCode = stateCode;
            this.name = name;
            this.regionCode = regionCode;
            this.population = population;
        }

        public String getStateCode() {
            return stateCode;
        }

        public void setStateCode(String stateCode) {
            this.stateCode = stateCode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public int getPopulation() {
            return population;
        }

        public void setPopulation(int population) {
            this.population = population;
        }

        @Override
        public String toString() {
            return "State{" +
                    "stateCode='" + stateCode + '\'' +
                    ", name='" + name + '\'' +
                    ", regionCode='" + regionCode + '\'' +
                    ", population=" + population +
                    '}';
        }
    }

}
