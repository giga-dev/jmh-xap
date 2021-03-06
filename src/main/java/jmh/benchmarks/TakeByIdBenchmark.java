package jmh.benchmarks;

import jmh.model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import jmh.utils.GigaSpaceFactory;

import java.rmi.RemoteException;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class TakeByIdBenchmark {

    @Benchmark
    public Object testTakeById(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.takeById(Message.class, String.valueOf(threadParams.getThreadIndex()));
    }


    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;

        private GigaSpace gigaSpace;

        @Setup
        public void setup() {
            gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));
            gigaSpace.clear(null);
        }

        @Setup(Level.Invocation)
        public void doWrite(ThreadParams threadParams) {
            gigaSpace.write(new Message().setId(String.valueOf(threadParams.getThreadIndex())).setPayload("foo"));
        }

        @TearDown
        public void teardown() {
            if (mode.equals(MODE_EMBEDDED)) {
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
                .include(TakeByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
