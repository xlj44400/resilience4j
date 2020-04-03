/*
 * Copyright 2020 Ingyu Hwang
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

package io.github.resilience4j.timelimiter.autoconfigure;

import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetricsPublisher;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({MeterRegistry.class, TimeLimiter.class, TaggedTimeLimiterMetricsPublisher.class})
@AutoConfigureAfter({MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(value = "resilience4j.timelimiter.metrics.enabled", matchIfMissing = true)
public class TimeLimiterMetricsAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "resilience4j.timelimiter.metrics.legacy.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public TaggedTimeLimiterMetrics registerTimeLimiterMetrics(TimeLimiterRegistry timeLimiterRegistry) {
        return TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(timeLimiterRegistry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(value = "resilience4j.timelimiter.metrics.legacy.enabled", havingValue = "false", matchIfMissing = true)
    @ConditionalOnMissingBean
    public TaggedTimeLimiterMetricsPublisher taggedTimeLimiterMetricsPublisher(MeterRegistry meterRegistry) {
        return new TaggedTimeLimiterMetricsPublisher(meterRegistry);
    }

}
