package sample;

import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.DataEventSessionFactory;
import com.gigaspaces.events.EventSessionConfig;
import com.gigaspaces.events.NotifyActionType;
import com.j_spaces.core.client.EntryArrivedRemoteEvent;
import model.Message;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import utils.GigaSpaceFactory;

import java.rmi.RemoteException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static utils.DefaultProperties.*;

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
    public static class ThreadState implements RemoteEventListener {

        private int threadIndex;
        private DataEventSession dataEventSession;
        private EventRegistration eventRegistration;
        //two parties trip the barrier - notify thread and write thread
        private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

        @Setup
        public void setup(SpaceState spaceState, ThreadParams threadParams) throws RunnerException {
            this.threadIndex = threadParams.getThreadIndex();
            registerEventListener(spaceState);
        }

        @TearDown
        public void teardown() {
            unRegisterEventListener();
        }

        private void registerEventListener(SpaceState spaceState) throws RunnerException {
            final EventSessionConfig config = new EventSessionConfig();
            dataEventSession = DataEventSessionFactory.create(spaceState.gigaSpace.getSpace(), config);
            try {
                eventRegistration = dataEventSession.addListener(
                        new Message().setId(String.valueOf(threadIndex)),
                        this, NotifyActionType.NOTIFY_WRITE_OR_UPDATE);
            } catch (RemoteException e) {
                throw new RunnerException("Failed to register event listener", e);
            }
        }

        private void unRegisterEventListener() {
            if (dataEventSession != null && eventRegistration != null) {
                try {
                    dataEventSession.removeListener(eventRegistration);
                } catch (Exception e) {
                    System.out.println("failed to remove listener, exception ignored " + e);
                }
            }
        }

        public void notify(RemoteEvent remoteEvent) throws RemoteException {
            EntryArrivedRemoteEvent event = ((EntryArrivedRemoteEvent) remoteEvent);
            try {
                Message msg = ((Message) event.getObject());
                if (msg != null) {
                    cyclicBarrier.await();
                } else {
                    throw new IllegalStateException("remoteEvent.getObject is null");
                }
            } catch (Exception e) {
                System.err.println("Caught exception waiting on barrier: " + e);
                throw new RemoteException("Caught exception waiting on barrier", e);
            }
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NotifyWriteOrUpdateBenchmark.class.getName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
