package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ReadByIdBenchmark;
import sample.ReadByIdsBenchmark;
import utils.Assertions;

import static utils.DefaultProperties.*;

public class CompareReadByIdVsReadByIdsTest {

    @Test
    public void remotes() throws RunnerException {
        Options readById = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options readByIds = new OptionsBuilder()
                .include(ReadByIdsBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(readById).run(),
                new Runner(readByIds).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options readById = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options readByIds = new OptionsBuilder()
                .include(ReadByIdsBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(readById).run(),
                new Runner(readByIds).run());
    }

}
