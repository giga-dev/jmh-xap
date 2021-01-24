package options.remote;

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
    public void remote() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param("mode", "remote")
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(25)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(18633.285, 19002.748, 19332.972, 177.494),
                new Runner(opt).run());
    }

    @Test
    public void remote_4_threads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param("mode", "remote")
                .threads(4)
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(25)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(46998.906, 48995.602, 49876.066, 625.967),
                new Runner(opt).run());
    }
}
