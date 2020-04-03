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

package io.github.resilience4j.timelimiter.configure;

import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorTimeLimiterAspectExt implements TimeLimiterAspectExt{

    /**
     * @param returnType the AOP method return type class
     * @return boolean if the method has Reactor return type
     */
    @Override
    public boolean canHandleReturnType(Class<?> returnType) {
        return Flux.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);
    }

    /**
     * handle the Spring web flux (Flux /Mono) return types AOP based into reactor time limiter
     * See {@link TimeLimiter} for details.
     *
     * @param proceedingJoinPoint Spring AOP proceedingJoinPoint
     * @param timeLimiter         the configured rateLimiter
     * @param methodName          the method name
     * @return the result object
     * @throws Throwable exception in case of faulty flow
     */
    @Override
    public Object handle(ProceedingJoinPoint proceedingJoinPoint,
        TimeLimiter timeLimiter, String methodName) throws Throwable {
        Object returnValue = proceedingJoinPoint.proceed();
        if (Flux.class.isAssignableFrom(returnValue.getClass())) {
            Flux<?> fluxReturnValue = (Flux<?>) returnValue;
            return fluxReturnValue.compose(TimeLimiterOperator.of(timeLimiter));
        } else if (Mono.class.isAssignableFrom(returnValue.getClass())) {
            Mono<?> monoReturnValue = (Mono<?>) returnValue;
            return monoReturnValue.compose(TimeLimiterOperator.of(timeLimiter));
        } else {
            throw new IllegalReturnTypeException(returnValue.getClass(), methodName,
                "Reactor expects Mono/Flux.");
        }
    }

}
