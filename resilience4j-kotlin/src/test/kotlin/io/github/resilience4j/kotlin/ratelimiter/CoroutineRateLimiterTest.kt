/*
 *
 *  Copyright 2019: Brad Newman
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.resilience4j.kotlin.ratelimiter

import io.github.resilience4j.kotlin.CoroutineHelloWorldService
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.Duration

class CoroutineRateLimiterTest {

    private fun noWaitConfig() = RateLimiterConfig
        .custom()
        .limitRefreshPeriod(Duration.ofSeconds(10))
        .limitForPeriod(10)
        .timeoutDuration(Duration.ZERO)
        .build()

    @Test
    fun `should execute successful function`() {
        runBlocking {
            val rateLimiter = RateLimiter.of("testName", noWaitConfig())
            val metrics = rateLimiter.metrics
            val helloWorldService = CoroutineHelloWorldService()

            //When
            val result = rateLimiter.executeSuspendFunction {
                helloWorldService.returnHelloWorld()
            }

            //Then
            Assertions.assertThat(result).isEqualTo("Hello world")
            Assertions.assertThat(metrics.availablePermissions).isEqualTo(9)
            Assertions.assertThat(metrics.numberOfWaitingThreads).isEqualTo(0)
            // Then the helloWorldService should be invoked 1 time
            Assertions.assertThat(helloWorldService.invocationCounter).isEqualTo(1)
        }
    }

    @Test
    fun `should execute unsuccessful function`() {
        runBlocking {
            val rateLimiter = RateLimiter.of("testName", noWaitConfig())
            val metrics = rateLimiter.metrics
            val helloWorldService = CoroutineHelloWorldService()

            //When
            try {
                rateLimiter.executeSuspendFunction {
                    helloWorldService.throwException()
                }
                Assertions.failBecauseExceptionWasNotThrown<Nothing>(IllegalStateException::class.java)
            } catch (e: IllegalStateException) {
                // nothing - proceed
            }

            //Then
            Assertions.assertThat(metrics.availablePermissions).isEqualTo(9)
            Assertions.assertThat(metrics.numberOfWaitingThreads).isEqualTo(0)
            // Then the helloWorldService should be invoked 1 time
            Assertions.assertThat(helloWorldService.invocationCounter).isEqualTo(1)
        }
    }

    @Test
    fun `should not execute function when rate limit reached and no waiting is allowed`() {
        runBlocking {
            val rateLimiter = RateLimiter.of("testName", noWaitConfig())
            val metrics = rateLimiter.metrics
            val helloWorldService = CoroutineHelloWorldService()

            for (i in 0 until 10) {
                rateLimiter.executeSuspendFunction {
                    helloWorldService.returnHelloWorld()
                }
            }

            //When
            try {
                rateLimiter.executeSuspendFunction {
                    helloWorldService.returnHelloWorld()
                }
                Assertions.failBecauseExceptionWasNotThrown<Nothing>(RequestNotPermitted::class.java)
            } catch (e: RequestNotPermitted) {
                // nothing - proceed
            }

            //Then
            Assertions.assertThat(metrics.availablePermissions).isEqualTo(0)
            Assertions.assertThat(metrics.numberOfWaitingThreads).isEqualTo(0)
            // Then the helloWorldService should not be invoked after the initial 10 times to use up the permits
            Assertions.assertThat(helloWorldService.invocationCounter).isEqualTo(10)
        }
    }

    @Test
    fun `should decorate successful function`() {
        runBlocking {
            val rateLimiter = RateLimiter.of("testName", noWaitConfig())
            val metrics = rateLimiter.metrics
            val helloWorldService = CoroutineHelloWorldService()

            //When
            val function = rateLimiter.decorateSuspendFunction {
                helloWorldService.returnHelloWorld()
            }

            //Then
            Assertions.assertThat(function()).isEqualTo("Hello world")
            Assertions.assertThat(metrics.availablePermissions).isEqualTo(9)
            Assertions.assertThat(metrics.numberOfWaitingThreads).isEqualTo(0)
            // Then the helloWorldService should be invoked 1 time
            Assertions.assertThat(helloWorldService.invocationCounter).isEqualTo(1)
        }
    }
}
