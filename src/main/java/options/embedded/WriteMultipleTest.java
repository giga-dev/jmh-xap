package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.WriteMultipleBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;
import static utils.DefaultProperties.MEASUREMENT_ITERATIONS_DEFAULT;

public class WriteMultipleTest {
    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteMultipleBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(656126.884, 662840.696, 667392.435, 4118.121),
                new Runner(opt).run());
    }
}
