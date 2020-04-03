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
package io.github.resilience4j.bulkhead.configure;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * BulkHead aspect extension support interface type if you want to support new types
 */
public interface BulkheadAspectExt {

    boolean canHandleReturnType(Class returnType);

	Object handle(ProceedingJoinPoint proceedingJoinPoint, Bulkhead bulkhead, String methodName)
        throws Throwable;
}
