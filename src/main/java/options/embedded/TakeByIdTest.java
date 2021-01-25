package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.TakeByIdBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class TakeByIdTest {

    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(947750.383, 995660.699, 1025990.352, 39098.791),
                new Runner(opt).run());
    }
}
