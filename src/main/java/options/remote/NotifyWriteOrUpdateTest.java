package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.NotifyWriteOrUpdateBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class NotifyWriteOrUpdateTest {

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NotifyWriteOrUpdateBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .threads(1)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(8852.490, 10000.680, 10896.093, 806.828),
                new Runner(opt).run());
    }

    @Test
    public void remote_2_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NotifyWriteOrUpdateBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .threads(2)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(16688.879, 17019.725, 17245.607, 155.878),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NotifyWriteOrUpdateBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(FORKS_DEFAULT)
                .threads(4)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(23936.427, 25478.198, 26188.070, 674.012),
                new Runner(opt).run());
    }
}
