/*
 * Copyright 2017 Robert Winkler
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
package io.github.resilience4j.circuitbreaker.configure;

import org.springframework.core.Ordered;

public class CircuitBreakerConfigurationProperties extends
    io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties {

    private int circuitBreakerAspectOrder = Ordered.LOWEST_PRECEDENCE - 3;

    /**
     * As of release 0.16.0 as we set an implicit spring aspect order now which is retry then
     * circuit breaker then rate limiter then bulkhead but user can override it still if he has
     * different use case but bulkhead will be first aspect all the time due to the implicit order
     * we have it for bulkhead
     */
    public int getCircuitBreakerAspectOrder() {
        return circuitBreakerAspectOrder;
    }

    /**
     * @param circuitBreakerAspectOrder circuit breaker aspect order
     */
    public void setCircuitBreakerAspectOrder(int circuitBreakerAspectOrder) {
        this.circuitBreakerAspectOrder = circuitBreakerAspectOrder;
    }


}
