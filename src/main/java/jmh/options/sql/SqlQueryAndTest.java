package jmh.options.sql;

import jmh.benchmarks.sql.SqlQueryAndBenchmark;
import jmh.utils.Assertions;
import jmh.utils.BaselineStatistics;
import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static jmh.utils.DefaultProperties.*;

public class SqlQueryAndTest {


    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(205816.878, 208565.775, 211025.199, 2397.474),
                new Runner(opt).run());
    }

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(8719.308, 8814.424, 8877.938, 72.119),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(17075.319, 17187.795, 17297.010, 102.669),
                new Runner(opt).run());
    }
}
