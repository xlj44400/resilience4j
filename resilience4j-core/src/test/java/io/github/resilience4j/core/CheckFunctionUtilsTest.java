package io.github.resilience4j.core;

import io.vavr.CheckedFunction0;
import org.junit.Test;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CheckFunctionUtilsTest {

    @Test
    public void shouldRecoverFromException() throws Throwable {
        CheckedFunction0<String> callable = () -> {
            throw new IOException("BAM!");
        };
        CheckedFunction0<String> callableWithRecovery = CheckFunctionUtils.recover(callable, (ex) -> "Bla");

        String result = callableWithRecovery.apply();

        assertThat(result).isEqualTo("Bla");
    }

    @Test
    public void shouldRecoverFromSpecificExceptions() throws Throwable {
        CheckedFunction0<String> callable = () -> {
            throw new IOException("BAM!");
        };

        CheckedFunction0<String> callableWithRecovery = CheckFunctionUtils.recover(callable,
            asList(IllegalArgumentException.class, IOException.class),
            (ex) -> "Bla");

        String result = callableWithRecovery.apply();

        assertThat(result).isEqualTo("Bla");
    }

    @Test
    public void shouldRecoverFromResult() throws Throwable {
        CheckedFunction0<String> callable = () -> "Wrong Result";

        CheckedFunction0<String> callableWithRecovery = CheckFunctionUtils.andThen(callable, (result, ex) -> {
            if(result.equals("Wrong Result")){
                return "Bla";
            }
            return result;
        });

        String result = callableWithRecovery.apply();

        assertThat(result).isEqualTo("Bla");
    }

    @Test
    public void shouldRecoverFromException2() throws Throwable {
        CheckedFunction0<String> callable = () -> {
            throw new IllegalArgumentException("BAM!");
        };
        CheckedFunction0<String> callableWithRecovery = CheckFunctionUtils.andThen(callable, (result, ex) -> {
            if(ex instanceof IllegalArgumentException){
                return "Bla";
            }
            return result;
        });

        String result = callableWithRecovery.apply();

        assertThat(result).isEqualTo("Bla");
    }

    @Test
    public void shouldRecoverFromSpecificResult() throws Throwable {
        CheckedFunction0<String> supplier = () -> "Wrong Result";

        CheckedFunction0<String> callableWithRecovery = CheckFunctionUtils.recover(supplier, (result) -> result.equals("Wrong Result"), (r) -> "Bla");
        String result = callableWithRecovery.apply();

        assertThat(result).isEqualTo("Bla");
    }


    @Test(expected = RuntimeException.class)
    public void shouldRethrowException() throws Throwable {
        CheckedFunction0<String> callable = () -> {
            throw new IOException("BAM!");
        };
        CheckedFunction0<String> callableWithRecovery = CheckFunctionUtils.recover(callable, (ex) -> {
            throw new RuntimeException();
        });

        callableWithRecovery.apply();
    }

    @Test(expected = RuntimeException.class)
    public void shouldRethrowException2() throws Throwable {
        CheckedFunction0<String> callable = () -> {
            throw new RuntimeException("BAM!");
        };
        CheckedFunction0<String> callableWithRecovery = CheckFunctionUtils.recover(callable, IllegalArgumentException.class, (ex) -> "Bla");

        callableWithRecovery.apply();
    }
}
