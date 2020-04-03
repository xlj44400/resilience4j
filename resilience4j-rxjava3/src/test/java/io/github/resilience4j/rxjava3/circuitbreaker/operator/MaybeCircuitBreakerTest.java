package io.github.resilience4j.rxjava3.circuitbreaker.operator;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.reactivex.rxjava3.core.Maybe;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Unit test for {@link MaybeCircuitBreaker}.
 */
public class MaybeCircuitBreakerTest extends BaseCircuitBreakerTest {

    @Test
    public void shouldSubscribeToMaybeJust() {
        given(circuitBreaker.tryAcquirePermission()).willReturn(true);

        Maybe.just(1)
            .compose(CircuitBreakerOperator.of(circuitBreaker))
            .test()
            .assertResult(1);

        then(circuitBreaker).should().onSuccess(anyLong(), any(TimeUnit.class));
        then(circuitBreaker).should(never())
            .onError(anyLong(), any(TimeUnit.class), any(Throwable.class));
    }

    @Test
    public void shouldPropagateError() {
        given(circuitBreaker.tryAcquirePermission()).willReturn(true);

        Maybe.error(new IOException("BAM!"))
            .compose(CircuitBreakerOperator.of(circuitBreaker))
            .test()
            .assertError(IOException.class)
            .assertNotComplete();

        then(circuitBreaker).should()
            .onError(anyLong(), any(TimeUnit.class), any(IOException.class));
        then(circuitBreaker).should(never()).onSuccess(anyLong(), any(TimeUnit.class));
    }


    @Test
    public void shouldEmitErrorWithCallNotPermittedException() {
        given(circuitBreaker.tryAcquirePermission()).willReturn(false);

        Maybe.just(1)
            .compose(CircuitBreakerOperator.of(circuitBreaker))
            .test()
            .assertError(CallNotPermittedException.class)
            .assertNotComplete();

        then(circuitBreaker).should(never()).onSuccess(anyLong(), any(TimeUnit.class));
        then(circuitBreaker).should(never())
            .onError(anyLong(), any(TimeUnit.class), any(Throwable.class));
    }

    @Test
    public void shouldReleasePermissionOnCancel() {
        given(circuitBreaker.tryAcquirePermission()).willReturn(true);

        Maybe.just(1)
            .delay(1, TimeUnit.DAYS)
            .compose(CircuitBreakerOperator.of(circuitBreaker))
            .test()
            .dispose();

        then(circuitBreaker).should().releasePermission();
        then(circuitBreaker).should(never())
            .onError(anyLong(), any(TimeUnit.class), any(Throwable.class));
        then(circuitBreaker).should(never()).onSuccess(anyLong(), any(TimeUnit.class));
    }

}
