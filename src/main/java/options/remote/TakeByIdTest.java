package options.remote;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.TakeByIdBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

public class TakeByIdTest {

    @Test
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeByIdBenchmark.class.getName())
                .param("mode", "remote")
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(25)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(17023.266, 17553.047, 17923.291, 231.504),
                new Runner(opt).run());
    }
}
