package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ReadByIdUsingSQLIdQueryBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class ReadByIdUsingSQLIdQueryTest {

    @Test
    public void embedded() throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(ReadByIdUsingSQLIdQueryBenchmark.class.getName())
                .param("mode", "embedded")
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(1512538.392, 1654055.980, 1773511.848, 70484.104),
                new Runner(opt).run());
    }
}
