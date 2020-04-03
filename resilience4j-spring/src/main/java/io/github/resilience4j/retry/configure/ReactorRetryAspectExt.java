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
package io.github.resilience4j.retry.configure;

import io.github.resilience4j.reactor.retry.RetryOperator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * the Reactor Retry logic support for the spring AOP Conditional on Reactor class existence on
 * spring class loader
 */
public class ReactorRetryAspectExt implements RetryAspectExt {

    private static final Logger logger = LoggerFactory.getLogger(ReactorRetryAspectExt.class);

    /**
     * @param returnType the AOP method return type class
     * @return boolean if the method has Reactor return type
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean canHandleReturnType(Class returnType) {
        return (Flux.class.isAssignableFrom(returnType)) || (Mono.class
            .isAssignableFrom(returnType));
    }

    /**
     * handle the Spring web flux (Flux /Mono) return types AOP based into reactor retry See {@link
     * io.github.resilience4j.retry.Retry} for details.
     *
     * @param proceedingJoinPoint Spring AOP proceedingJoinPoint
     * @param retry               the configured retry
     * @param methodName          the method name
     * @return the result object
     * @throws Throwable exception in case of faulty flow
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object handle(ProceedingJoinPoint proceedingJoinPoint,
        io.github.resilience4j.retry.Retry retry, String methodName) throws Throwable {
        Object returnValue = proceedingJoinPoint.proceed();
        if (Flux.class.isAssignableFrom(returnValue.getClass())) {
            Flux<?> fluxReturnValue = (Flux<?>) returnValue;
            return fluxReturnValue.compose(RetryOperator.of(retry));
        } else if (Mono.class.isAssignableFrom(returnValue.getClass())) {
            Mono<?> monoReturnValue = (Mono<?>) returnValue;
            return monoReturnValue.compose(RetryOperator.of(retry));
        } else {
            logger.error("Unsupported type for Reactor retry {}",
                returnValue.getClass().getTypeName());
            throw new IllegalArgumentException(
                "Not Supported type for the retry in Reactor :" + returnValue.getClass().getName());

        }
    }
}
