package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ChangeUsingSetAndSQLIdQueryBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class ChangeUsingSetAndSQLIdQueryTest {

    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ChangeUsingSetAndSQLIdQueryBenchmark.class.getName())
                .param("mode", "embedded")
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(813708.635, 990134.486, 1043515.205, 68001.957),
                new Runner(opt).run());
    }
}
