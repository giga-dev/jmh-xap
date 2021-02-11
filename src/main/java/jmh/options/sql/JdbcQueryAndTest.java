package jmh.options.sql;

import jmh.benchmarks.sql.JdbcQueryAndBenchmark;
import jmh.utils.Assertions;
import jmh.utils.BaselineStatistics;
import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static jmh.utils.DefaultProperties.*;

public class JdbcQueryAndTest {


    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(174717.657, 187825.496, 192662.753, 7395.111),
                new Runner(opt).run());
    }

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(9832.076, 9877.375, 9962.545, 50.946),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(19241.672, 19432.932, 19617.809, 153.757),
                new Runner(opt).run());
    }
}
