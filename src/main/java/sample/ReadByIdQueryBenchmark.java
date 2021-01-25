package sample;

import com.gigaspaces.query.IdQuery;
import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;
import java.util.Random;

import static utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class ReadByIdQueryBenchmark {

    @Param({MODE_EMBEDDED, MODE_REMOTE})
    private static String mode;

    @Benchmark
    public Object testReadByIdQuery(SpaceState spaceState) {
        return spaceState.gigaSpace.readById(new IdQuery<Message>(Message.class, spaceState.getKey()));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        private final Random random = new Random();
        private int threadsCount;
        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            gigaSpace.clear(null);
            threadsCount = benchmarkParams.getThreads();
            for(int i = 0 ; i < threadsCount ; i++) {
                gigaSpace.write(new Message().setId(String.valueOf(i)).setPayload("foo"));
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

        public String getKey() {
            return String.valueOf(random.nextInt(threadsCount));
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadByIdQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}