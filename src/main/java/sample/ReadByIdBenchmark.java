package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.DefaultProperties;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;
import java.util.Random;

@State(Scope.Benchmark)
public class ReadByIdBenchmark {

    @Param({"embedded", "remote"})
    private static String mode;

    @Benchmark
    public Object testReadById(SpaceState spaceState) {
        return spaceState.gigaSpace.readById(Message.class, spaceState.getKey());
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        private final Random random = new Random();
        private int threadsCount;
        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DefaultProperties.DEFAULT_SPACE_NAME, mode.equals("embedded"));

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            gigaSpace.clear(null);
            threadsCount = benchmarkParams.getThreads();
            for(int i = 0 ; i < threadsCount ; i++) {
                gigaSpace.write(new Message().setId(String.valueOf(i)).setPayload("foo"));
            }
        }

        @TearDown
        public void teardown() {
            if (mode.equals("embedded")) {
                try {
                    gigaSpace.getSpace().getDirectProxy().shutdown();
                } catch (RemoteException e) {
                    System.err.println("failed to shutdown Space" + e);
                }
            }
        }

        public String getKey() {
            return String.valueOf(random.nextInt(threadsCount));
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .param("mode", "embedded")
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
