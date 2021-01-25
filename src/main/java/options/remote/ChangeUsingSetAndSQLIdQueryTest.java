package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ChangeUsingSetAndSQLIdQueryBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class ChangeUsingSetAndSQLIdQueryTest {

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ChangeUsingSetAndSQLIdQueryBenchmark.class.getName())
                .param("mode", "remote")
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(13496.654, 15728.413, 16229.494, 597.123),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ChangeUsingSetAndSQLIdQueryBenchmark.class.getName())
                .param("mode", "remote")
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(24754.901, 26153.910, 27339.699, 771.890),
                new Runner(opt).run());
    }

    @Test
    public void remote_8_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ChangeUsingSetAndSQLIdQueryBenchmark.class.getName())
                .param("mode", "remote")
                .threads(8)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(31162.713, 31930.235, 32338.223, 337.898),
                new Runner(opt).run());
    }
}
