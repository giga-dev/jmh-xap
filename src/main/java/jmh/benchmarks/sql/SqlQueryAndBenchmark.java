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
import org.openspaces.core.GigaSpace;

import java.rmi.RemoteException;

import static jmh.utils.DefaultProperties.*;
import static jmh.utils.DefaultProperties.MODE_EMBEDDED;

@State(Scope.Benchmark)
public class SqlQueryAndBenchmark {

    @Benchmark
    public Object testSqlQueryAnd(SpaceState spaceState, ThreadState threadState, Blackhole bh) throws Exception {
        Person[] people = spaceState.gigaSpace.readMultiple(threadState.getQuery());
        bh.consume(threadState.validateResults(people));
        return people;
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

    // Each thread write his own object and then read this object using SQLQuery.
    @State(Scope.Thread)
    public static class ThreadState {

        @Param({"false"})
        private boolean enableValidation;

        private final SQLQuery<Person> query = new SQLQuery<>(Person.class, "salary >= ? AND salary <= ?");
        private double minSalary;
        private double maxSalary;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) {
            int threadIndex = threadParams.getThreadIndex();
            String name = String.valueOf(threadIndex);
            spaceStat.gigaSpace.write(
                    new Person()
                            .setId(threadIndex)
                            .setFirstName(name)
                            .setLastName(name)
                            .setSalary((double) (threadIndex))
            );

            double rangeFactor = 0.5d;
            this.minSalary = threadIndex - rangeFactor;
            this.maxSalary = threadIndex + rangeFactor;
            this.query.setParameters(this.minSalary, this.maxSalary);

            //TODO: try compare vs hardcoded query without parameters.
        }

        public SQLQuery<Person> getQuery() {
            return this.query;
        }

        public boolean validateResults(Person[] people) throws Exception {
            if (enableValidation) {
                if(people != null && people.length > 0){
                    Assert.assertEquals("results length should be 1", 1, people.length);
                    for (Person person : people){
                        if (person != null){
                            Assert.assertTrue("Wrong result returned", this.minSalary <= person.getSalary() && person.getSalary() <= this.maxSalary);
                        } else {
                            throw new Exception("person can't be null");
                        }
                    }
                    return true;
                } else {
                    throw new Exception("people can't be null or empty");
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlQueryAndBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
//                .warmupIterations(1).warmupTime(TimeValue.seconds(1))
//                .measurementIterations(1).measurementTime(TimeValue.seconds(1))
//                .param(PARAM_SQL_ENABLE_VALIDATION, SQL_ENABLE_VALIDATION)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}


//        # 4 objects in space:
//
//        # Warmup: 5 iterations, 10 s each
//        # Measurement: 5 iterations, 10 s each
//        # Threads: 4 threads, will synchronize iterations
//        # Parameters: (mode = remote)
//
//        Result "jmh.benchmarks.SqlQueryBenchmark.testSqlQueryRead":
//        18504.601 ±(99.9%) 579.309 ops/s [Average]
//        (min, avg, max) = (18241.556, 18504.601, 18602.875), stdev = 150.445
//        CI (99.9%): [17925.293, 19083.910] (assumes normal distribution)
//
//
//        Result "jmh.benchmarks.SqlQueryBenchmark.testSqlQueryReadMultiple":
//        17591.651 ±(99.9%) 1393.823 ops/s [Average]
//        (min, avg, max) = (16962.396, 17591.651, 17892.091), stdev = 361.971
//        CI (99.9%): [16197.829, 18985.474] (assumes normal distribution)
//
//
//        Benchmark                                   (mode)   Mode  Cnt      Score      Error  Units
//        SqlQueryBenchmark.testSqlQueryRead          remote  thrpt    5  18504.601 ±  579.309  ops/s
//        SqlQueryBenchmark.testSqlQueryReadMultiple  remote  thrpt    5  17591.651 ± 1393.823  ops/s

//        # 1,000,000 objects in space (same as GridGain):
//
//        # Warmup: 5 iterations, 10 s each
//        # Measurement: 5 iterations, 10 s each
//        # Threads: 4 threads, will synchronize iterations
//        # Parameters: (mode = remote)
//
//        Result "jmh.benchmarks.SqlQueryBenchmark.testSqlQueryRead":
//        17010.662 ±(99.9%) 498.101 ops/s [Average]
//        (min, avg, max) = (16809.585, 17010.662, 17119.169), stdev = 129.355
//        CI (99.9%): [16512.561, 17508.763] (assumes normal distribution)
//
//        Result "jmh.benchmarks.SqlQueryBenchmark.testSqlQueryReadMultiple":
//        16576.776 ±(99.9%) 476.486 ops/s [Average]
//        (min, avg, max) = (16449.576, 16576.776, 16775.734), stdev = 123.742
//        CI (99.9%): [16100.290, 17053.263] (assumes normal distribution)

//        Benchmark                                   (mode)   Mode  Cnt      Score     Error  Units
//        SqlQueryBenchmark.testSqlQueryRead          remote  thrpt    5  17010.662 ± 498.101  ops/s
//        SqlQueryBenchmark.testSqlQueryReadMultiple  remote  thrpt    5  16576.776 ± 476.486  ops/s
//
//        Process finished with exit code 0

