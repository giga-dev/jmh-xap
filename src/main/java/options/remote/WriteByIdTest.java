package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.WriteByIdBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class WriteByIdTest {

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();


        Assertions.assertResults(
                new BaselineStatistics(16131.971, 17616.408, 17886.991, 393.313),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();


        Assertions.assertResults(
                new BaselineStatistics(16131.971, 17616.408, 17886.991, 393.313),
                new Runner(opt).run());
    }

    @Test
    public void remote_8_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(8)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();


        Assertions.assertResults(
                new BaselineStatistics(16131.971, 17616.408, 17886.991, 393.313),
                new Runner(opt).run());
    }
}
