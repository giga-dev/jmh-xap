package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openspaces.core.GigaSpace;

import utils.GigaSpaceFactory;

import java.rmi.RemoteException;

import static utils.DefaultProperties.*;
import static utils.DefaultProperties.MODE_EMBEDDED;

@State(Scope.Benchmark)
public class TakeByIdsBenchmark {

    @Benchmark
    public Object testTakeByIds(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.takeByIds(Message.class, threadState.getThreadIds());
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
        private Message[] threadMessageObjects;
        @Param({DEFAULT_OBJECT_COUNT})
        private int threadObjectCount;

        // Each Thread writes 'threadObjectCount' to space, and TakeByIds those objects.
        @Setup
        public void setup(ThreadParams threadParams) {
            this.threadIds = new String[threadObjectCount];
            this.threadMessageObjects = new Message[threadObjectCount];

            int from = threadParams.getThreadIndex() * threadObjectCount;
            int to = from + threadObjectCount;
            for (int i = from, index = 0 ; i < to ; i++, index++) {
                String id = String.valueOf(i);
                this.threadIds[index] = id;
                this.threadMessageObjects[index] = new Message().setId(id).setPayload("foo");
            }
        }

        @Setup(Level.Invocation)
        public void doWrite(SpaceState spaceStat) {
            spaceStat.gigaSpace.writeMultiple(this.threadMessageObjects);
        }

        public String[] getThreadIds() {
            return threadIds;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TakeByIdsBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .param(PARAM_OBJECT_COUNT, DEFAULT_OBJECT_COUNT)
                .threads(4)
                .warmupIterations(5).warmupTime(TimeValue.seconds(10))
                .measurementIterations(5).measurementTime(TimeValue.seconds(10))
                .forks(1)
                .build();
        new Runner(opt).run();
    }

}
