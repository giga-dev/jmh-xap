package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.DefaultProperties;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;

@State(Scope.Benchmark)
public class WriteByIdBenchmark {

    @Param({"embedded", "remote"})
    private static String mode;

    @Benchmark
    public Object testWriteById(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.write(new Message().setId(String.valueOf(threadParams.getThreadIndex())).setPayload("foo"));
    }


    @State(Scope.Benchmark)
    public static class SpaceState {

        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DefaultProperties.DEFAULT_SPACE_NAME, mode.equals("embedded"));

        @Setup
        public void setup() {
            gigaSpace.clear(null);
        }

        @Setup(Level.Invocation)
        public void doTake(ThreadParams threadParams) {
            gigaSpace.takeById(Message.class, String.valueOf(threadParams.getThreadIndex()));
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
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteByIdBenchmark.class.getName())
                .param("mode", "embedded")
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
