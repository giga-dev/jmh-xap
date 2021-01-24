package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.WriteByIdBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

public class WriteByIdTest {

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteByIdBenchmark.class.getName())
                .param("mode", "remote")
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(25)
                .build();


        Assertions.assertResults(
                new BaselineStatistics(16131.971, 17616.408, 17886.991, 393.313),
                new Runner(opt).run());
    }
}
