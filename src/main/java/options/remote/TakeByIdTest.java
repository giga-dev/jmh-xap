package options.remote;

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
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeByIdBenchmark.class.getName())
                .param("mode", "remote")
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(17023.266, 17553.047, 17923.291, 231.504),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeByIdBenchmark.class.getName())
                .param("mode", "remote")
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(17023.266, 17553.047, 17923.291, 231.504),
                new Runner(opt).run());
    }

    @Test
    public void remote_8_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeByIdBenchmark.class.getName())
                .param("mode", "remote")
                .threads(8)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(17023.266, 17553.047, 17923.291, 231.504),
                new Runner(opt).run());
    }
}
