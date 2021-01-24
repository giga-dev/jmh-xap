package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.UpdateAndReadByIdBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class UpdateAndReadByIdTest {

    @Test
    public void remote_1put_3get_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UpdateAndReadByIdBenchmark.class.getName())
                .param("mode", "remote")
                .threads(4)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(43506.679, 46463.806, 47466.763, 964.645),
                new Runner(opt).run());
    }
}
