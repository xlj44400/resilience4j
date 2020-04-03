/*
 * Copyright 2019 Mahmoud Romeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.bulkhead.autoconfigure;

import io.github.resilience4j.TestUtils;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.configure.BulkheadAspect;
import io.github.resilience4j.bulkhead.configure.BulkheadAspectExt;
import io.github.resilience4j.bulkhead.configure.BulkheadConfiguration;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.consumer.DefaultEventConsumerRegistry;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.fallback.FallbackDecorators;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    BulkheadConfigurationOnMissingBeanTest.ConfigWithOverrides.class,
    BulkheadAutoConfiguration.class,
    BulkheadConfigurationOnMissingBean.class
})
@EnableConfigurationProperties(BulkheadProperties.class)
public class BulkheadConfigurationOnMissingBeanTest {

    @Autowired
    private ConfigWithOverrides configWithOverrides;

    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Autowired
    private BulkheadAspect bulkheadAspect;

    @Autowired
    private EventConsumerRegistry<BulkheadEvent> bulkheadEventEventConsumerRegistry;

    @Test
    public void testAllBeansFromBulkHeadHasOnMissingBean() throws NoSuchMethodException {
        final Class<BulkheadConfiguration> originalClass = BulkheadConfiguration.class;
        final Class<BulkheadConfigurationOnMissingBean> onMissingBeanClass = BulkheadConfigurationOnMissingBean.class;
        TestUtils.assertAnnotations(originalClass, onMissingBeanClass);
    }

    @Test
    public void testAllBulkHeadConfigurationBeansOverridden() {
        assertEquals(bulkheadRegistry, configWithOverrides.bulkheadRegistry);
        assertEquals(bulkheadAspect, configWithOverrides.bulkheadAspect);
        assertEquals(bulkheadEventEventConsumerRegistry,
            configWithOverrides.bulkheadEventEventConsumerRegistry);
    }

    @Configuration
    public static class ConfigWithOverrides {

        private BulkheadRegistry bulkheadRegistry;

        private BulkheadAspect bulkheadAspect;

        private EventConsumerRegistry<BulkheadEvent> bulkheadEventEventConsumerRegistry;

        @Bean
        public BulkheadRegistry bulkheadRegistry() {
            bulkheadRegistry = BulkheadRegistry.ofDefaults();
            return bulkheadRegistry;
        }

        @Bean
        public BulkheadAspect bulkheadAspect(BulkheadRegistry bulkheadRegistry,
            ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
            @Autowired(required = false) List<BulkheadAspectExt> bulkheadAspectExts,
            FallbackDecorators fallbackDecorators) {
            bulkheadAspect = new BulkheadAspect(new BulkheadProperties(),
                threadPoolBulkheadRegistry, bulkheadRegistry, bulkheadAspectExts,
                fallbackDecorators);
            return bulkheadAspect;
        }

        @Bean
        public EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry() {
            bulkheadEventEventConsumerRegistry = new DefaultEventConsumerRegistry<>();
            return bulkheadEventEventConsumerRegistry;
        }
    }
}