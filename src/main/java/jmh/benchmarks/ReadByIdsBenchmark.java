package jmh.benchmarks;

import jmh.model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openspaces.core.GigaSpace;
import jmh.utils.GigaSpaceFactory;

import java.rmi.RemoteException;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class ReadByIdsBenchmark {

    @Benchmark
    public Object testReadByIds(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.readByIds(Message.class, threadState.getThreadIds());
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;
        //initialization of shared state requires gigaspaces to be initialized at @Setup
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

        private String[] threadIds;

        // Each Thread writes 1 object to space, and ReadByIds this object.
        @Setup
        public void setup(SpaceState spaceState, ThreadParams threadParams) {
            String id = String.valueOf(threadParams.getThreadIndex());
            this.threadIds = new String[]{id};
            spaceState.gigaSpace.write(new Message().setId(id).setPayload("foo"));
        }

        public String[] getThreadIds() {
            return threadIds;
        }
    }

        public static void main(String[] args) throws RunnerException {

            Options opt = new OptionsBuilder()
                    .include(ReadByIdsBenchmark.class.getName())
                    .param(PARAM_MODE, MODE_REMOTE)
                    .threads(4)
                    .warmupIterations(5).warmupTime(TimeValue.seconds(10))
                    .measurementIterations(5).measurementTime(TimeValue.seconds(10))
                    .forks(1)
                    .build();

            new Runner(opt).run();
    }
}
