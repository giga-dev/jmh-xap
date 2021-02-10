package jmh.benchmarks.sql;

import com.j_spaces.core.client.SQLQuery;
import jmh.model.Person;
import jmh.utils.GigaSpaceFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;

import java.rmi.RemoteException;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class UpdateAndSqlQueryAndBenchmark {

    @Benchmark
    @Group("PutGetSqlQuery")
    @GroupThreads(1)
    public Object testWrite(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.write(threadState.getPersonObject());
    }

    @Benchmark
    @Group("PutGetSqlQuery")
    @GroupThreads(3)
    public Object testSqlQueryAnd(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.readMultiple(threadState.getQuery());
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;

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

    // Each thread write his own object and then read this object using SQLQuery
    // or write it again.
    @State(Scope.Thread)
    public static class ThreadState {

        private final SQLQuery<Person> query = new SQLQuery<Person>(Person.class,"salary >= ? AND salary <= ?");
        private Person personObject;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) {
            int threadIndex = threadParams.getThreadIndex();
            String name = String.valueOf(threadIndex);
            this.personObject = new Person()
                    .setId(threadIndex)
                    .setFirstName(name)
                    .setLastName(name)
                    .setSalary((double) (threadIndex));
            spaceStat.gigaSpace.write(this.personObject);

            double rangeFactor = 0.5d;
            this.query.setParameters(threadIndex - rangeFactor , threadIndex + rangeFactor);
        }

        public SQLQuery<Person> getQuery() {
            return this.query;
        }

        public Person getPersonObject() {
            return this.personObject;
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UpdateAndSqlQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
