package sample;

import model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;

import static utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class ReadByIdsBenchmark {

    @Benchmark
    public Object testReadByIds(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.readByIds(Message.class, threadState.getThreadIds());
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;
        //initialization of shared state requires gigaspaces to be initialized at @Setup
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

        private String[] threadIds;
        @Param({DEFAULT_OBJECT_COUNT})
        private int threadObjectCount;

        // Each Thread writes 'threadObjectCount' to space, and ReadByIds those objects.
        @Setup
        public void setup(SpaceState spaceState, ThreadParams threadParams) {
            this.threadIds = new String[threadObjectCount];
            Message[] messages = new Message[threadObjectCount];

            //serial ids, for example 2 thread and 2 objects per thread:
            //threadIndex 0 --> ids 0, 1
            //threadIndex 1 --> ids 2 ,3
            int from = threadParams.getThreadIndex() * threadObjectCount;
            int to = from + threadObjectCount;
            for (int i = from, index = 0 ; i < to ; i++, index++) {
                String id = String.valueOf(i);
                this.threadIds[index] = id;
                messages[index] = new Message().setId(id).setPayload("foo");
            }
            //modulo ids, for example 2 thread and 2 objects per thread:
            //threadIndex 0 --> ids 0, 2
            //threadIndex 1 --> ids 1, 3
//            int totalSpaceObjects = threadParams.getThreadCount() * threadObjectCount;
//            for (int i = 0, index = 0 ; i < totalSpaceObjects ; i++) {
//                if (i % threadParams.getThreadCount() == threadParams.getThreadIndex()) {
//                    String id = String.valueOf(i);
//                    this.threadIds[index] = id;
//                    messages[index] = new Message().setId(id).setPayload("foo");
//                    index++;
//                }
//
//            }

            spaceState.gigaSpace.writeMultiple(messages);
        }

        public String[] getThreadIds() {
            return threadIds;
        }
    }



        public static void main(String[] args) throws RunnerException {

            Options opt = new OptionsBuilder()
                    .include(ReadByIdsBenchmark.class.getName())
                    .param(PARAM_MODE, MODE_REMOTE)
                    .threads(4)
                    .warmupIterations(5).warmupTime(TimeValue.seconds(10))
                    .measurementIterations(5).measurementTime(TimeValue.seconds(10))
                    .forks(1)
                    .build();

            new Runner(opt).run();
    }
}
