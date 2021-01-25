package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.UpdateAndReadByIdBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class UpdateAndReadByIdTest {

    @Test
    public void embedded_1put_3get_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UpdateAndReadByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(2377205.645, 2700933.810, 2769548.833, 73893.452),
                new Runner(opt).run());
    }
}
