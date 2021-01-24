package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
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
        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace("rgtest", mode.equals("embedded"));

        @Setup
        public void setup() {
            gigaSpace.clear(null);
            gigaSpace.write(new Message().setId("1").setPayload("foo"));
            gigaSpace.write(new Message().setId("2").setPayload("bar"));
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
            return String.valueOf(Math.abs(random.nextInt(2)+1));
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
