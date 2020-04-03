/*
 * Copyright 2019 Ingyu Hwang
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

package io.github.resilience4j.metrics.publisher;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.codahale.metrics.MetricRegistry.name;
import static io.github.resilience4j.ratelimiter.utils.MetricNames.*;
import static java.util.Objects.requireNonNull;

public class RateLimiterMetricsPublisher extends AbstractMetricsPublisher<RateLimiter> {

    private final String prefix;

    public RateLimiterMetricsPublisher() {
        this(DEFAULT_PREFIX, new MetricRegistry());
    }

    public RateLimiterMetricsPublisher(MetricRegistry metricRegistry) {
        this(DEFAULT_PREFIX, metricRegistry);
    }

    public RateLimiterMetricsPublisher(String prefix, MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.prefix = requireNonNull(prefix);
    }

    @Override
    public void publishMetrics(RateLimiter rateLimiter) {
        String name = rateLimiter.getName();

        String waitingThreads = name(prefix, name, WAITING_THREADS);
        String availablePermissions = name(prefix, name, AVAILABLE_PERMISSIONS);

        metricRegistry.register(waitingThreads,
            (Gauge<Integer>) rateLimiter.getMetrics()::getNumberOfWaitingThreads);
        metricRegistry.register(availablePermissions,
            (Gauge<Integer>) rateLimiter.getMetrics()::getAvailablePermissions);

        List<String> metricNames = Arrays.asList(waitingThreads, availablePermissions);
        metricsNameMap.put(name, new HashSet<>(metricNames));
    }

    @Override
    public void removeMetrics(RateLimiter rateLimiter) {
        removeMetrics(rateLimiter.getName());
    }

}
