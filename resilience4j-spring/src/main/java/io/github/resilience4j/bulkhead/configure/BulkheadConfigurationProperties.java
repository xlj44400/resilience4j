/*
 * Copyright 2019 lespinsideg
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
package io.github.resilience4j.bulkhead.configure;

import org.springframework.core.Ordered;

public class BulkheadConfigurationProperties extends
    io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigurationProperties {

    /**
     * As of release 0.16.0 as we set an implicit spring aspect order for bulkhead to cover the
     * async case of threadPool bulkhead
     */
    public int getBulkheadAspectOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
