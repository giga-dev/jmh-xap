package options.embedded;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sample.ChangeUsingSetAndIdTemplateBenchmark;
import utils.Assertions;
import utils.BaselineStatistics;

import static utils.DefaultProperties.*;

public class ChangeUsingSetAndIdTemplateTest {

    @Test
    public void embedded() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ChangeUsingSetAndIdTemplateBenchmark.class.getName())
                .param("mode", "embedded")
                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .forks(FORKS_DEFAULT)
                .warmupIterations(WARMUP_ITERATIONS_DEFAULT)
                .measurementIterations(MEASUREMENT_ITERATIONS_DEFAULT)
                .build();

        Assertions.assertResults(
                new BaselineStatistics(830148.693, 923591.223, 997224.350, 48198.572),
                new Runner(opt).run());
    }
}
