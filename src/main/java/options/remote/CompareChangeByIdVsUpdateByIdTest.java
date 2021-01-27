package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ChangeByIdQuerySetBenchmark;
import sample.UpdateByIdBenchmark;
import utils.Assertions;

import static utils.DefaultProperties.*;
import static utils.DefaultProperties.FORKS_DEFAULT;

public class CompareChangeByIdVsUpdateByIdTest {

    @Test
    public void remote() throws RunnerException {
        Options updateById = new OptionsBuilder()
                .include(UpdateByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options changeByIdQuerySet = new OptionsBuilder()
                .include(ChangeByIdQuerySetBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(updateById).run(),
                new Runner(changeByIdQuerySet).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options updateById = new OptionsBuilder()
                .include(UpdateByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Options changeByIdQuerySet = new OptionsBuilder()
                .include(ChangeByIdQuerySetBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(updateById).run(),
                new Runner(changeByIdQuerySet).run());
    }
}
