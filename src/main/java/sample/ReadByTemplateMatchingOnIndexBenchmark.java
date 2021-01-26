package sample;

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
import java.util.concurrent.ThreadLocalRandom;

import static utils.DefaultProperties.*;
import static utils.DefaultProperties.MODE_EMBEDDED;

@State(Scope.Benchmark)
public class ReadByTemplateMatchingOnIndexBenchmark {

    @Param({MODE_EMBEDDED, MODE_REMOTE})
    private static String mode;

    @Benchmark
    public Object testReadByTemplateMatchingOnIndex(SpaceState spaceState) {
        return spaceState.gigaSpace.read(new Book()
                .setAuthor(spaceState.getAuthor()));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        private int threadsCount;
        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            gigaSpace.clear(null);
            threadsCount = benchmarkParams.getThreads();
            for(int i = 0 ; i < threadsCount ; i++) {
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

        public String getAuthor() {
            return String.valueOf(ThreadLocalRandom.current().nextInt(threadsCount));
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadByTemplateMatchingOnIndexBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}