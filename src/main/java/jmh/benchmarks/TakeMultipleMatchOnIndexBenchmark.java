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
import static jmh.utils.DefaultProperties.MODE_REMOTE;

@State(Scope.Benchmark)
public class TakeMultipleMatchOnIndexBenchmark {

    // Each thread takes 1 object from space.
    @Benchmark
    public Object testTakeMultipleMatchOnIndex(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.takeMultiple(threadState.getTemplate(), 1);
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;

        private  GigaSpace gigaSpace;

        @Setup
        public void setup() {
            gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));
            gigaSpace.clear(null);
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

    @State(Scope.Thread)
    public static class ThreadState {

        private Message threadMessageObject;

        @Setup
        public void setup(ThreadParams threadParams) {
            String id = String.valueOf(threadParams.getThreadIndex());
            this.threadMessageObject = new Message().setId(id).setPayload("foo");
        }

        @Setup(Level.Invocation)
        public void doWrite(SpaceState spaceStat) {
            spaceStat.gigaSpace.write(this.threadMessageObject);
        }

        public Message getTemplate() {
            return this.threadMessageObject;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeMultipleMatchOnIndexBenchmark.class.getName())
//                .param(PARAM_MODE, MODE_EMBEDDED)
//                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
