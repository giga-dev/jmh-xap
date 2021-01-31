package sample;

import com.gigaspaces.client.ChangeSet;
import model.Book;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;

import static utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class ChangeByTemplateMatchingOnIndexSetBenchmark {

    @Benchmark
    public Object testChangeByTemplateMatchingOnIndexSet(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.change(
                new Book().setAuthor(String.valueOf(threadParams.getThreadIndex())),
                new ChangeSet().set("payload", "bar"));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;

        private GigaSpace gigaSpace;

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));
            gigaSpace.clear(null);
            for(int i = 0 ; i < benchmarkParams.getThreads() ; i++) {
                gigaSpace.write(new Book()
                        .setId(String.valueOf(i))
                        .setAuthor(String.valueOf(i))
                        .setPayload("foo"));
            }
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
                .include(ChangeByTemplateMatchingOnIndexSetBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(1)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
