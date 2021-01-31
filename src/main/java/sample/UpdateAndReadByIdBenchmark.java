package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;


import static utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class UpdateAndReadByIdBenchmark {

    @Param({MODE_EMBEDDED, MODE_REMOTE})
    private static String mode;

    @Benchmark
    @Group("PutGet")
    @GroupThreads(1)
    public Object testPut(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.write(new Message().setId(String.valueOf(threadParams.getThreadIndex())).setPayload("zoo"));
    }

    @Benchmark
    @Group("PutGet")
    @GroupThreads(3)
    public Object testGet(SpaceState spaceState, ThreadParams threadParams) {
        return spaceState.gigaSpace.readById(Message.class, String.valueOf(threadParams.getThreadIndex()));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            gigaSpace.clear(null);
            for(int i = 0 ; i < benchmarkParams.getThreads() ; i++){
                gigaSpace.write(new Message().setId(String.valueOf(i)).setPayload("foo"));

            }
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UpdateAndReadByIdBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
