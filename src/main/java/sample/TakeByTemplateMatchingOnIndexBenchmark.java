package sample;

import model.Book;
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
import static utils.DefaultProperties.MODE_EMBEDDED;

@State(Scope.Benchmark)
public class TakeByTemplateMatchingOnIndexBenchmark {

    @Param({MODE_EMBEDDED, MODE_REMOTE})
    private static String mode;

    @Benchmark
    public Object testTakeByTemplateMatchingOnIndex(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.take(new Book()
                .setAuthor(String.valueOf(threadParams.getThreadIndex())));
    }


    @State(Scope.Benchmark)
    public static class SpaceState {

        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));

        @Setup
        public void setup() {
            gigaSpace.clear(null);
        }

        @Setup(Level.Invocation)
        public void doWrite(ThreadParams threadParams) {
            gigaSpace.write(new Book()
                    .setId(String.valueOf(threadParams.getThreadIndex()))
                    .setAuthor(String.valueOf(threadParams.getThreadIndex()))
                    .setPayload("foo"));
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
                .include(TakeByTemplateMatchingOnIndexBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
