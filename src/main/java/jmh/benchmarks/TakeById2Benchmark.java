package jmh.benchmarks;

import jmh.model.Message;
import jmh.utils.GigaSpaceFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;

import java.rmi.RemoteException;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class TakeById2Benchmark {

    @Benchmark
    public Object testTakeById(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.takeById(Message.class, threadState.getThreadId());
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

        private String threadId;
        private Message threadMessageObject;

        // Each Thread writes 1 object to space, and TakeById this object.
        @Setup
        public void setup(ThreadParams threadParams) {
            String id = String.valueOf(threadParams.getThreadIndex());
            this.threadId = id;
            this.threadMessageObject = new Message().setId(id).setPayload("foo");
        }

        @Setup(Level.Invocation)
        public void doWrite(SpaceState spaceStat) {
            spaceStat.gigaSpace.write(this.threadMessageObject);
        }

        public String getThreadId() {
            return threadId;
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeById2Benchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
