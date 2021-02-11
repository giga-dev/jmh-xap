package jmh.options.sql;

import jmh.benchmarks.sql.SqlQuerySubQueryBenchmark;
import jmh.utils.Assertions;
import jmh.utils.BaselineStatistics;
import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static jmh.utils.DefaultProperties.*;

public class SqlQuerySubQueryTest {


    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQuerySubQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(97421.388, 99619.326, 101605.154, 1881.358),
                new Runner(opt).run());
    }

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQuerySubQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(4391.054, 4448.133, 4468.744, 32.283),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQuerySubQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(8688.910, 8776.585, 8876.611, 68.839),
                new Runner(opt).run());
    }
}
