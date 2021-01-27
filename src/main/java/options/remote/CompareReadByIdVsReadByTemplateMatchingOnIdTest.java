package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ReadByIdBenchmark;
import sample.ReadByTemplateMatchingOnIdBenchmark;
import utils.Assertions;

import static utils.DefaultProperties.*;

public class CompareReadByIdVsReadByTemplateMatchingOnIdTest {

    @Test
    public void remote() throws RunnerException {
        Options readById = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options readByTemplateMatchingOnId = new OptionsBuilder()
                .include(ReadByTemplateMatchingOnIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(readById).run(),
                new Runner(readByTemplateMatchingOnId).run());
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

        Options readByTemplateMatchingOnId = new OptionsBuilder()
                .include(ReadByTemplateMatchingOnIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(readById).run(),
                new Runner(readByTemplateMatchingOnId).run());
    }
}
