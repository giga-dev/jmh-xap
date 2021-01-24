package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.util.Random;

@State(Scope.Benchmark)
public class UpdateAndReadByIdBenchmark {

    @Param({"embedded", "remote"})
    private static String mode;

    @Benchmark
    @Group("PutGet")
    @GroupThreads(1)
    public Object testPut(SpaceState spaceState) {
        return spaceState.gigaSpace.write(new Message().setId(spaceState.getKey()).setPayload("zoo"));
    }

    @Benchmark
    @Group("PutGet")
    @GroupThreads(3)
    public Object testGet(SpaceState spaceState) {
        return spaceState.gigaSpace.readById(Message.class, spaceState.getKey());
    }

    @State(Scope.Benchmark)
    public static class SpaceState {
        private final Random random = new Random();
        private final GigaSpace gigaSpace = GigaSpaceFactory.getOrCreateSpace("rgtest", mode.equals("embedded"));

        @Setup
        public void setup() {
            gigaSpace.clear(null);
            gigaSpace.write(new Message().setId("1").setPayload("foo"));
            gigaSpace.write(new Message().setId("2").setPayload("bar"));
            gigaSpace.write(new Message().setId("3").setPayload("car"));
            gigaSpace.write(new Message().setId("4").setPayload("zoo"));
        }

        public String getKey() {
            return String.valueOf(Math.abs(random.nextInt(4)+1));
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UpdateAndReadByIdBenchmark.class.getName())
                .param("mode", "embedded")
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
