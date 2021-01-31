package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;

import static utils.DefaultProperties.*;
import static utils.DefaultProperties.MODE_REMOTE;

@State(Scope.Benchmark)
public class ReadMultipleByTemplateMatchingOnIdBenchmark {

    // Each thread reads 1 object from space.
    @Benchmark
    public Object testReadMultipleByTemplateMatchingOnId(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.readMultiple(threadState.getTemplate(), 1);
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

        private Message threadMessageObject;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) {
            String id = String.valueOf(threadParams.getThreadIndex());
            this.threadMessageObject = new Message().setId(id).setPayload("foo");
            spaceStat.gigaSpace.write(this.threadMessageObject);
        }

        public Message getTemplate() {
            return this.threadMessageObject;
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadMultipleByTemplateMatchingOnIdBenchmark.class.getName())
//                .param(PARAM_MODE, MODE_EMBEDDED)
//                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
