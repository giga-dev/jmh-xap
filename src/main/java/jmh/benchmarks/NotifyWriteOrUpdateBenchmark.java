package jmh.benchmarks;

import jmh.model.Message;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import jmh.utils.GigaSpaceFactory;

import java.rmi.RemoteException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static jmh.utils.DefaultProperties.*;

/**
 * Benchmark for Notify with WRITE_OR_UPDATE interest.
 * - Each thread registers a listener on it's own thread index.
 * - Each thread writes/updates on it's own index and waits for the notification to arrive
 * - The time measured for each write until a notify event is received
 *
 * This is done through a shared state (ThreadState) which holds a cyclic barrier that the
 * write operation waits on until the notify event trips the barrier.
 *
 * @author Moran
 */
@State(Scope.Benchmark)
public class NotifyWriteOrUpdateBenchmark {

    @Benchmark
    public Object testNotifyWriteOrUpdate(SpaceState spaceState, ThreadState threadState)
            throws InterruptedException, BrokenBarrierException, TimeoutException {

        spaceState.gigaSpace.write(
                new Message()
                        .setId(String.valueOf(threadState.threadIndex))
                        .setPayload("foo"));

        //wait for the notification to arrive - or throw TimeoutException
        return threadState.cyclicBarrier.await(1, TimeUnit.MINUTES);
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

        private int threadIndex;
        //two parties trip the barrier - notify thread and write thread
        private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        private SimpleNotifyEventListenerContainer eventListener;

        @Setup
        public void setup(SpaceState spaceState, ThreadParams threadParams) throws RunnerException {
            this.threadIndex = threadParams.getThreadIndex();
            this.registerEventListener(spaceState);
        }

        @TearDown
        public void teardown() {
            this.unRegisterEventListener();
        }

        private void registerEventListener(SpaceState spaceState) throws RunnerException {
            eventListener  = new SimpleNotifyContainerConfigurer(spaceState.gigaSpace)
                    .notifyWrite(true)
                    .notifyUpdate(true)
                    .template(new Message().setId(String.valueOf(threadIndex)))
                    .eventListenerAnnotation(new Object(){
                        @SpaceDataEvent
                        public Message notify(Message message) throws BrokenBarrierException, InterruptedException {
                            cyclicBarrier.await();
                            return null;
                        }
                    })
                    .notifyContainer();

            eventListener.start();
        }

        private void unRegisterEventListener() {
            if (eventListener != null) {
                eventListener.destroy();
            }
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NotifyWriteOrUpdateBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
