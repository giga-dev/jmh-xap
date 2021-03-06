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

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class UpdateAndSqlQueryAndBenchmark {

    @Benchmark
    @Group("PutGetSqlQueryAnd")
    @GroupThreads(1)
    public Object testWrite(SpaceState spaceState, ThreadState threadState) {
        return spaceState.gigaSpace.write(threadState.getPersonObject());
    }

    @Benchmark
    @Group("PutGetSqlQueryAnd")
    @GroupThreads(3)
    public Object testSqlQueryAnd(SpaceState spaceState, ThreadState threadState, Blackhole bh) throws Exception {
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

    // Each thread write his own object and then read this object using SQLQuery
    // or write it again.
    @State(Scope.Thread)
    public static class ThreadState {

        private final SQLQuery<Person> query = new SQLQuery<>(Person.class,"salary >= ? AND salary <= ?");
        private boolean enableValidation;
        private double minSalary;
        private double maxSalary;
        private int threadIndex;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) {
            this.enableValidation = spaceStat.enableValidation;
            this.threadIndex = threadParams.getThreadIndex();
            String name = String.valueOf(this.threadIndex);
            spaceStat.gigaSpace.write(
                    new Person()
                            .setId(this.threadIndex)
                            .setFirstName(name)
                            .setLastName(name)
                            .setSalary((double) (this.threadIndex))
            );

            double rangeFactor = 0.5d;
            this.minSalary = this.threadIndex - rangeFactor;
            this.maxSalary = this.threadIndex + rangeFactor;
            this.query.setParameters(this.minSalary, this.maxSalary);
        }

        public SQLQuery<Person> getQuery() {
            return this.query;
        }

        public Person getPersonObject() {
            String name = String.valueOf(this.threadIndex);
            return  new Person()
                    .setId(this.threadIndex)
                    .setFirstName(name)
                    .setLastName(name)
                    .setSalary((double) (this.threadIndex));
        }

        public boolean validateResults(Person[] people) throws Exception {
            if (this.enableValidation) {
                if(people != null && people.length > 0){
                    Assert.assertEquals("results length should be 1", 1, people.length);
                    for (Person person : people){
                        Assert.assertTrue("wrong result returned",
                                this.minSalary <= person.getSalary() && person.getSalary() <= this.maxSalary);
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
                .include(UpdateAndSqlQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_EMBEDDED)
//                .warmupIterations(1).warmupTime(TimeValue.seconds(1))
//                .measurementIterations(1).measurementTime(TimeValue.seconds(1))
                .param(PARAM_SQL_ENABLE_VALIDATION, SQL_ENABLE_VALIDATION)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
