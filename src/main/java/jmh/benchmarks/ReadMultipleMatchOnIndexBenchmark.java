package jmh.benchmarks;

import jmh.model.Book;
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
public class ReadMultipleMatchOnIndexBenchmark {

    // Each thread reads 1 object from space.
    @Benchmark
    public Object testReadMultipleMatchOnIndex(SpaceState spaceState, ThreadState threadState) {
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

        private Book threadBookTemplate;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) {
            String id = String.valueOf(threadParams.getThreadIndex());
            this.threadBookTemplate = new Book().setAuthor(id);
            spaceStat.gigaSpace.write(new Book().setId(id).setAuthor(id).setPayload("foo"));
        }

        public Book getTemplate() {
            return this.threadBookTemplate;
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadMultipleMatchOnIndexBenchmark.class.getName())
//                .param(PARAM_MODE, MODE_EMBEDDED)
//                .jvmArgs(JVM_ARGS_EMBEDDED_DEFAULT)
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
