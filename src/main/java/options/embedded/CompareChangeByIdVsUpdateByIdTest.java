package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ChangeByIdQuerySetBenchmark;
import sample.UpdateByIdBenchmark;
import utils.Assertions;

import static utils.DefaultProperties.*;

public class CompareChangeByIdVsUpdateByIdTest {

    @Test
    public void embedded() throws RunnerException {
        Options updateById = new OptionsBuilder()
                .include(UpdateByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options changeByIdQuerySet = new OptionsBuilder()
                .include(ChangeByIdQuerySetBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(updateById).run(),
                new Runner(changeByIdQuerySet).run());
    }
}
