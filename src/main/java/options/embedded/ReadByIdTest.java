package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ReadByIdBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

public class ReadByIdTest {

    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param("mode", "embedded")
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(25)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(1936645.252, 2001733.752, 2059101.215, 34104.486),
                new Runner(opt).run());
    }
}
