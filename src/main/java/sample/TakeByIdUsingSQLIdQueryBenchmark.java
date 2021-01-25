package sample;

import com.gigaspaces.query.IdQuery;
import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.DefaultProperties;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;

import static utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class TakeByIdUsingSQLIdQueryBenchmark {

    @Param({MODE_EMBEDDED, MODE_REMOTE})
    private static String mode;

    @Benchmark
    public Object testTakeByIdUsingSQLIdQuery(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.takeById(new IdQuery<Message>(Message.class, threadParams.getThreadIndex()));
    }


    @State(Scope.Benchmark)
    public static class SpaceState {

        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DefaultProperties.DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));

        @Setup
        public void setup() {
            gigaSpace.clear(null);
        }

        @Setup(Level.Invocation)
        public void doWrite(ThreadParams threadParams) {
            gigaSpace.write(new Message().setId(String.valueOf(threadParams.getThreadIndex())).setPayload("foo"));
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
                .include(TakeByIdUsingSQLIdQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
