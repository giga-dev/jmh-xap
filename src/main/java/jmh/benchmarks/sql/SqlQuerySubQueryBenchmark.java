package jmh.benchmarks.sql;

import com.j_spaces.core.client.SQLQuery;
import jmh.model.Organization;
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
public class SqlQuerySubQueryBenchmark {

    @Benchmark
    public Object testSqlSubQuery(SpaceState spaceState, ThreadState threadState, Blackhole bh) throws Exception {
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
        private final SQLQuery<Person> query = new SQLQuery<>(Person.class,
                "organizationId = " +
                        "(SELECT o.id " +
                        "FROM " + Organization.class.getName() + " AS o " +
                        "WHERE o.name = ?) " +
                        "AND salary >= ? AND salary <= ?");
        private double minSalary;
        private double maxSalary;
        private String name;
        private int threadIndex;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) {
            this.enableValidation = spaceStat.enableValidation;
            this.threadIndex = threadParams.getThreadIndex();
            this.name = String.valueOf(this.threadIndex);
            spaceStat.gigaSpace.write(
                    new Person()
                            .setId(this.threadIndex)
                            .setOrganizationId(this.threadIndex)
                            .setFirstName(this.name)
                            .setLastName(this.name)
                            .setSalary((double) (this.threadIndex))
            );
            spaceStat.gigaSpace.write(
                    new Organization()
                            .setId(this.threadIndex)
                            .setName(this.name)
            );

            double rangeFactor = 0.5d;
            this.minSalary = this.threadIndex - rangeFactor;
            this.maxSalary = this.threadIndex + rangeFactor;
            this.query.setParameters(this.name, this.minSalary, this.maxSalary);
        }

        public SQLQuery<Person> getQuery() {
            return this.query;
        }

        public boolean validateResults(Person[] people) throws Exception {
            if (this.enableValidation) {
                if(people != null && people.length > 0){
                    Assert.assertEquals("results length should be 1", 1, people.length);
                    for (Person person : people){
                        Assert.assertTrue("wrong result returned",
                                this.minSalary <= person.getSalary()
                                        && person.getSalary() <= this.maxSalary
                                        && Integer.parseInt(this.name) == person.getOrganizationId());
                    }
                    return true;
                } else {
                    throw new Exception("results of thread [" + this.threadIndex + "] are null or empty!");
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQuerySubQueryBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
//                .warmupIterations(1).warmupTime(TimeValue.seconds(1))
//                .measurementIterations(1).measurementTime(TimeValue.seconds(1))
                .param(PARAM_SQL_ENABLE_VALIDATION, SQL_ENABLE_VALIDATION)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}