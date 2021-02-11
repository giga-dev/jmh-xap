package jmh.options.sql;

import jmh.benchmarks.sql.JdbcQueryInnerJoinBenchmark;
import jmh.utils.Assertions;
import jmh.utils.BaselineStatistics;
import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static jmh.utils.DefaultProperties.*;

public class JdbcQueryInnerJoinTest {


    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryInnerJoinBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(56295.624, 56704.542, 57163.947, 371.286),
                new Runner(opt).run());
    }

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryInnerJoinBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(1)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(9513.466, 9619.354, 9675.339, 61.638),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryInnerJoinBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(16850.185, 18576.819, 19203.406, 978.650),
                new Runner(opt).run());
    }
}
