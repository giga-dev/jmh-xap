package jmh.options.compare;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import jmh.benchmarks.ReadByIdBenchmark;
import jmh.benchmarks.ReadByIdQueryBenchmark;
import jmh.utils.Assertions;

import static jmh.utils.DefaultProperties.*;
import static jmh.utils.DefaultProperties.MEASUREMENT_ITERATIONS_DEFAULT;

public class CompareReadByIdVsReadByIdQueryTest {

    @Test
    public void remote() throws RunnerException {
        Options readByIdQuery = new OptionsBuilder()
                .include(ReadByIdQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options readById = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(readByIdQuery).run(),
                new Runner(readById).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options readByIdQuery = new OptionsBuilder()
                .include(ReadByIdQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options readById = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(readByIdQuery).run(),
                new Runner(readById).run());
    }
}
