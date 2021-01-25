package sample;

import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.query.IdQuery;
import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class ChangeByIdQuerySetBenchmark {

    @Param({MODE_EMBEDDED, MODE_REMOTE})
    private static String mode;

    @Benchmark
    public Object testChangeByIdQuerySet(SpaceState spaceState) {
        return spaceState.gigaSpace.change(new IdQuery<Message>(Message.class, spaceState.getKey()), new ChangeSet().set("payload", "bar"));
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
            return String.valueOf(ThreadLocalRandom.current().nextInt(threadsCount));
        }

    }
}
