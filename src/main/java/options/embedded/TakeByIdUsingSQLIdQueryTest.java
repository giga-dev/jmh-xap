package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.TakeByIdUsingSQLIdQueryBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class TakeByIdUsingSQLIdQueryTest {

    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeByIdUsingSQLIdQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(1772089.939, 1850562.256, 1944153.320, 72936.254),
                new Runner(opt).run());
    }
}
