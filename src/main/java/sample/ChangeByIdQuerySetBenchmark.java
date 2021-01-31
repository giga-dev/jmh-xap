package sample;

import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.query.IdQuery;
import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.ThreadParams;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;


import static utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class ChangeByIdQuerySetBenchmark {

    @Param({MODE_EMBEDDED, MODE_REMOTE})
    private static String mode;

    @Benchmark
    public Object testChangeByIdQuerySet(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.change(
                new IdQuery<Message>(Message.class, String.valueOf(threadParams.getThreadIndex())),
                new ChangeSet().set("payload", "bar"));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            gigaSpace.clear(null);
            for(int i = 0 ; i < benchmarkParams.getThreads() ; i++) {
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
    }
}
