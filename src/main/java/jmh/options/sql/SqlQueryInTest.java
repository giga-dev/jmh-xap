package jmh.options.sql;

import jmh.benchmarks.sql.SqlQueryInBenchmark;
import jmh.utils.Assertions;
import jmh.utils.BaselineStatistics;
import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static jmh.utils.DefaultProperties.*;

public class SqlQueryInTest {


    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryInBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(330272.437, 332816.655, 335773.345, 2255.914),
                new Runner(opt).run());
    }

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryInBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(7071.535, 7304.498, 7381.179, 131.663),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryInBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(13618.411, 13854.895, 13998.272, 151.390),
                new Runner(opt).run());
    }
}
