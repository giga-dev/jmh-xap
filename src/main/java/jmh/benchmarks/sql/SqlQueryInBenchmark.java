package jmh.benchmarks.sql;

import com.j_spaces.core.client.SQLQuery;
import jmh.model.Person;
import jmh.utils.GigaSpaceFactory;
import org.junit.Assert;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openspaces.core.GigaSpace;

import java.rmi.RemoteException;
import java.util.HashSet;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class SqlQueryInBenchmark {

    @Benchmark
    public Object testSqlQueryIn(SpaceState spaceState, ThreadState threadState, Blackhole bh) throws Exception {
        Person[] people = spaceState.gigaSpace.readMultiple(threadState.getQuery());
        bh.consume(threadState.validateResults(people));
        return people;
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;
        @Param({"false"})
        private boolean enableValidation;

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

    // Each thread write his own object and then read this object using SQLQuery.
    @State(Scope.Thread)
    public static class ThreadState {

        private boolean enableValidation;
        private final SQLQuery<Person> query = new SQLQuery<>(Person.class, "id IN (?)");
        private HashSet<Integer> idsSet;
        private int threadIndex;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) {
            this.enableValidation = spaceStat.enableValidation;
            this.idsSet = new HashSet<>();
            for(int i = 0; i < threadParams.getThreadCount(); i++) {
                this.idsSet.add(i);
            }

            this.threadIndex = threadParams.getThreadIndex();
            String name = String.valueOf(this.threadIndex);
            spaceStat.gigaSpace.write(
                    new Person()
                            .setId(this.threadIndex)
                            .setFirstName(name)
                            .setLastName(name)
                            .setSalary((double) (this.threadIndex))
            );

            this.query.setParameter(1, this.idsSet);
        }

        public SQLQuery<Person> getQuery() {
            return this.query;
        }

        public boolean validateResults(Person[] people) throws Exception {
            if (this.enableValidation) {
                if(people != null && people.length > 0){
                    Assert.assertEquals(String.format("results length should be %d", this.idsSet.size()), this.idsSet.size(), people.length);
                    for (Person person : people){
                        Assert.assertTrue("wrong result returned",
                                this.idsSet.contains(person.getId()));
                    }
                    return true;
                } else {
                    throw new Exception("results of thread [" + this.threadIndex + "] are null or empty");
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryInBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .warmupIterations(1).warmupTime(TimeValue.seconds(1))
                .measurementIterations(1).measurementTime(TimeValue.seconds(1))
                .param(PARAM_SQL_ENABLE_VALIDATION, SQL_ENABLE_VALIDATION)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

