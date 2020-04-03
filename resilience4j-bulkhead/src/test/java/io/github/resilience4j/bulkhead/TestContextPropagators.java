package io.github.resilience4j.bulkhead;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestContextPropagators {

    public static class TestThreadLocalContextPropagator implements ContextPropagator<String> {
        private ThreadLocal<String> threadLocal;

        public TestThreadLocalContextPropagator(ThreadLocal<String> threadLocal) {
            this.threadLocal = threadLocal;
        }

        @Override
        public Supplier<Optional<String>> retrieve() {
            return () -> (Optional<String>) Optional.ofNullable(threadLocal.get());
        }

        @Override
        public Consumer<Optional<String>> copy() {
            return (t) -> t.ifPresent(e -> {
                if (threadLocal.get() != null) {
                    threadLocal.set(null);
                    threadLocal.remove();
                }
                threadLocal.set(e);
            });
        }

        @Override
        public Consumer<Optional<String>> clear() {
            return (t) -> {
                if (threadLocal.get() != null) {
                    threadLocal.set(null);
                    threadLocal.remove();
                }
            };
        }
    }

    public static class TestThreadLocalContextPropagatorWithHolder<T> implements ContextPropagator<T> {

        @Override
        public Supplier<Optional<T>> retrieve() {
            return () -> (Optional<T>) TestThreadLocalContextHolder.get();
        }

        @Override
        public Consumer<Optional<T>> copy() {
            return (t) -> t.ifPresent(e -> {
                clear();
                TestThreadLocalContextHolder.put(e);
            });
        }

        @Override
        public Consumer<Optional<T>> clear() {
            return t -> TestThreadLocalContextHolder.clear();
        }

        public static class TestThreadLocalContextHolder {

            private static final ThreadLocal<Object> threadLocal = new ThreadLocal<>();

            private TestThreadLocalContextHolder() {
            }

            public static void put(Object context) {
                if (threadLocal.get() != null) {
                    clear();
                }
                threadLocal.set(context);
            }

            public static void clear() {
                if (threadLocal.get() != null) {
                    threadLocal.set(null);
                    threadLocal.remove();
                }
            }

            public static Optional<?> get() {
                return Optional.ofNullable(threadLocal.get());
            }
        }
    }
}

