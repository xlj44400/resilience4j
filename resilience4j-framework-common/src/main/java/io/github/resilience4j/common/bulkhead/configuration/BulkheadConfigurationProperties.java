/*
 * Copyright 2019 Dan Maas , Mahmoud Romeh
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
package io.github.resilience4j.common.bulkhead.configuration;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.common.CommonProperties;
import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.utils.ConfigUtils;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.StringUtils;
import io.github.resilience4j.core.lang.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BulkheadConfigurationProperties extends CommonProperties {

    private Map<String, InstanceProperties> instances = new HashMap<>();
    private Map<String, InstanceProperties> configs = new HashMap<>();

    public BulkheadConfig createBulkheadConfig(InstanceProperties instanceProperties,
        CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer,
        String instanceName) {
        if (StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {
            InstanceProperties baseProperties = configs.get(instanceProperties.getBaseConfig());
            if (baseProperties == null) {
                throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
            }
            return buildConfigFromBaseConfig(baseProperties, instanceProperties,
                compositeBulkheadCustomizer, instanceName);
        }
        return buildBulkheadConfig(BulkheadConfig.custom(), instanceProperties,
            compositeBulkheadCustomizer, instanceName);
    }

    private BulkheadConfig buildConfigFromBaseConfig(InstanceProperties baseProperties,
        InstanceProperties instanceProperties,
        CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer,
        String instanceName) {
        ConfigUtils.mergePropertiesIfAny(baseProperties, instanceProperties);
        BulkheadConfig baseConfig = buildBulkheadConfig(BulkheadConfig.custom(), baseProperties,
            compositeBulkheadCustomizer, instanceName);
        return buildBulkheadConfig(BulkheadConfig.from(baseConfig), instanceProperties,
            compositeBulkheadCustomizer, instanceName);
    }

    private BulkheadConfig buildBulkheadConfig(BulkheadConfig.Builder builder,
        InstanceProperties instanceProperties,
        CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer,
        String instanceName) {
        if (instanceProperties.getMaxConcurrentCalls() != null) {
            builder.maxConcurrentCalls(instanceProperties.getMaxConcurrentCalls());
        }
        if (instanceProperties.getMaxWaitDuration() != null) {
            builder.maxWaitDuration(instanceProperties.getMaxWaitDuration());
        }
        compositeBulkheadCustomizer.getCustomizer(instanceName)
            .ifPresent(bulkheadConfigCustomizer -> bulkheadConfigCustomizer.customize(builder));
        return builder.build();
    }

    @Nullable
    public InstanceProperties getBackendProperties(String backend) {
        return instances.get(backend);
    }

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    /**
     * For backwards compatibility when setting backends in configuration properties.
     */
    public Map<String, InstanceProperties> getBackends() {
        return instances;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }

    /**
     * Bulkhead config adapter for integration with Ratpack. {@link #maxWaitDuration} should almost
     * always be set to 0, so the compute threads would not be blocked upon execution.
     */
    public static class InstanceProperties {

        private Integer maxConcurrentCalls;
        private Duration maxWaitDuration;
        @Nullable
        private String baseConfig;
        @Nullable
        private Integer eventConsumerBufferSize;

        public Integer getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public InstanceProperties setMaxConcurrentCalls(Integer maxConcurrentCalls) {
            Objects.requireNonNull(maxConcurrentCalls);
            if (maxConcurrentCalls < 1) {
                throw new IllegalArgumentException(
                    "maxConcurrentCalls must be greater than or equal to 1.");
            }

            this.maxConcurrentCalls = maxConcurrentCalls;
            return this;
        }

        public Duration getMaxWaitDuration() {
            return maxWaitDuration;
        }

        public InstanceProperties setMaxWaitDuration(Duration maxWaitDuration) {
            Objects.requireNonNull(maxWaitDuration);
            if (maxWaitDuration.toMillis() < 0) {
                throw new IllegalArgumentException(
                    "maxWaitDuration must be greater than or equal to 0.");
            }

            this.maxWaitDuration = maxWaitDuration;
            return this;
        }

        @Nullable
        public String getBaseConfig() {
            return baseConfig;
        }

        public InstanceProperties setBaseConfig(String baseConfig) {
            this.baseConfig = baseConfig;
            return this;
        }

        @Nullable
        public Integer getEventConsumerBufferSize() {
            return eventConsumerBufferSize;
        }

        public InstanceProperties setEventConsumerBufferSize(Integer eventConsumerBufferSize) {
            Objects.requireNonNull(eventConsumerBufferSize);
            if (eventConsumerBufferSize < 1) {
                throw new IllegalArgumentException(
                    "eventConsumerBufferSize must be greater than or equal to 1.");
            }

            this.eventConsumerBufferSize = eventConsumerBufferSize;
            return this;
        }

    }

}
