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

import io.github.resilience4j.core.lang.Nullable;
import io.github.resilience4j.fallback.FallbackDecorators;
import io.github.resilience4j.fallback.FallbackMethod;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.github.resilience4j.utils.AnnotationExtractor;
import io.github.resilience4j.utils.ValueResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.*;

@Aspect
public class TimeLimiterAspect implements EmbeddedValueResolverAware, Ordered, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TimeLimiterAspect.class);

    private final TimeLimiterRegistry timeLimiterRegistry;
    private final TimeLimiterConfigurationProperties properties;
    private static final ScheduledExecutorService timeLimiterExecutorService = Executors
        .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    @Nullable
    private final List<TimeLimiterAspectExt> timeLimiterAspectExtList;
    private final FallbackDecorators fallbackDecorators;
    private StringValueResolver embeddedValueResolver;

    public TimeLimiterAspect(TimeLimiterRegistry timeLimiterRegistry,
        TimeLimiterConfigurationProperties properties,
        @Nullable List<TimeLimiterAspectExt> timeLimiterAspectExtList,
        FallbackDecorators fallbackDecorators) {
        this.timeLimiterRegistry = timeLimiterRegistry;
        this.properties = properties;
        this.timeLimiterAspectExtList = timeLimiterAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
    }

    @Pointcut(value = "@within(timeLimiter) || @annotation(timeLimiter)", argNames = "timeLimiter")
    public void matchAnnotatedClassOrMethod(TimeLimiter timeLimiter) {
        // a marker method
    }

    @Around(value = "matchAnnotatedClassOrMethod(timeLimiterAnnotation)", argNames = "proceedingJoinPoint, timeLimiterAnnotation")
    public Object timeLimiterAroundAdvice(ProceedingJoinPoint proceedingJoinPoint,
        @Nullable TimeLimiter timeLimiterAnnotation) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if (timeLimiterAnnotation == null) {
            timeLimiterAnnotation = getTimeLimiterAnnotation(proceedingJoinPoint);
        }
        if(timeLimiterAnnotation == null) {
            return proceedingJoinPoint.proceed();
        }
        String name = timeLimiterAnnotation.name();
        io.github.resilience4j.timelimiter.TimeLimiter timeLimiter =
            getOrCreateTimeLimiter(methodName, name);
        Class<?> returnType = method.getReturnType();

        String fallbackMethodValue = ValueResolver.resolve(
            this.embeddedValueResolver, timeLimiterAnnotation.fallbackMethod());
        if (StringUtils.isEmpty(fallbackMethodValue)) {
            return proceed(proceedingJoinPoint, methodName, timeLimiter, returnType);
        }
        FallbackMethod fallbackMethod = FallbackMethod
            .create(fallbackMethodValue, method,
                proceedingJoinPoint.getArgs(), proceedingJoinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod,
            () -> proceed(proceedingJoinPoint, methodName, timeLimiter, returnType)).apply();
    }

    private Object proceed(ProceedingJoinPoint proceedingJoinPoint, String methodName,
        io.github.resilience4j.timelimiter.TimeLimiter timeLimiter, Class<?> returnType)
        throws Throwable {
        if (timeLimiterAspectExtList != null && !timeLimiterAspectExtList.isEmpty()) {
            for (TimeLimiterAspectExt timeLimiterAspectExt : timeLimiterAspectExtList) {
                if (timeLimiterAspectExt.canHandleReturnType(returnType)) {
                    return timeLimiterAspectExt.handle(proceedingJoinPoint, timeLimiter, methodName);
                }
            }
        }

        if (!CompletionStage.class.isAssignableFrom(returnType)) {
            throw new IllegalReturnTypeException(returnType, methodName,
                "CompletionStage expected.");
        }

        return handleJoinPointCompletableFuture(proceedingJoinPoint, timeLimiter);
    }

    private io.github.resilience4j.timelimiter.TimeLimiter getOrCreateTimeLimiter(String methodName, String name) {
        io.github.resilience4j.timelimiter.TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(name);

        if (logger.isDebugEnabled()) {
            TimeLimiterConfig timeLimiterConfig = timeLimiter.getTimeLimiterConfig();
            logger.debug(
                    "Created or retrieved time limiter '{}' with timeout duration '{}' and cancelRunningFuture '{}' for method: '{}'",
                    name, timeLimiterConfig.getTimeoutDuration(), timeLimiterConfig.shouldCancelRunningFuture(), methodName
            );
        }

        return timeLimiter;
    }

    @Nullable
    private static TimeLimiter getTimeLimiterAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
            logger.debug("The TimeLimiter annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor.extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), TimeLimiter.class);
        } else {
            return AnnotationExtractor.extract(proceedingJoinPoint.getTarget().getClass(), TimeLimiter.class);
        }
    }

    private static Object handleJoinPointCompletableFuture(
            ProceedingJoinPoint proceedingJoinPoint, io.github.resilience4j.timelimiter.TimeLimiter timeLimiter) throws Throwable {
        return timeLimiter.executeCompletionStage(timeLimiterExecutorService, () -> {
            try {
                return (CompletionStage<?>) proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }

    @Override
    public int getOrder() {
        return properties.getTimeLimiterAspectOrder();
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    public void close() throws Exception {
        timeLimiterExecutorService.shutdown();
        try {
            if (!timeLimiterExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                timeLimiterExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            if (!timeLimiterExecutorService.isTerminated()) {
                timeLimiterExecutorService.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }
    }
}
