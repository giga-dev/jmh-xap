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
                .param(PARAM_OBJECT_COUNT, SINGLE_OBJECT)
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
                .param(PARAM_OBJECT_COUNT, SINGLE_OBJECT)
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new Runner(readById).run(),
                new Runner(readByIds).run());
    }

//    remote_4_threads():
//    Assert results:
//    testReadById statistics:
//            (min, avg, max) = (33459.532, 33630.923, 33882.819), stdev = 195.766
//    CI (99.9%): [32877.099, 34384.746]
//
//    testReadByIds statistics:
//            (min, avg, max) = (19577.553, 19692.793, 19773.504), stdev = 80.919
//    CI (99.9%): [19381.203, 20004.383]
//
//    Throughput comparison results (maximum allowed deviation 5.0%):
//    Avg : testReadById > testReadByIds (33630.923/19692.793) = 70.778% - exceeds 5.000%
//    CI_0: testReadById > testReadByIds (32877.099/19381.203) = 69.634% - exceeds 5.000%
//    CI_1: testReadById > testReadByIds (34384.746/20004.383) = 71.886% - exceeds 5.000%
//    [x] Mean difference is statistically significant
//
//    java.lang.AssertionError: Deviation from mean is statistically significant

}
