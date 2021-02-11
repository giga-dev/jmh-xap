package jmh.benchmarks.sql;

import com.j_spaces.jdbc.driver.GConnection;
import jmh.model.Organization;
import jmh.model.Person;
import jmh.utils.Assertions;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class JdbcQueryAndBenchmark {

    @Benchmark
    public Object testJdbcQueryAnd(ThreadState threadState, Blackhole bh) throws Exception {
        try (ResultSet resultSet = threadState.getStatement().executeQuery()) {
            bh.consume(threadState.validateResults(resultSet));
            return resultSet;
        }
    }



    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;
        @Param({"false"})
        private boolean enableValidation;

        private GigaSpace gigaSpace;
        private Connection connection;
        private final Object mutex = new Object(); // mutex for 'connection' because its not thread safe.

        @Setup
        public void setup() throws SQLException {
            gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));
            gigaSpace.clear(null);
            this.connection = GConnection.getInstance(this.gigaSpace.getSpace());
        }

        @TearDown
        public void teardown() throws SQLException {
            if (this.connection != null) {
                this.connection.close();
            }

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

        private boolean enableValidation;
        private PreparedStatement preparedStatement;
        private double minSalary;
        private double maxSalary;
        private int threadIndex;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) throws SQLException {
            this.enableValidation = spaceStat.enableValidation;
            this.threadIndex = threadParams.getThreadIndex();
            String name = String.valueOf(this.threadIndex);
            spaceStat.gigaSpace.write(
                    new Person()
                            .setId(this.threadIndex)
                            .setOrganizationId(this.threadIndex)
                            .setFirstName(name)
                            .setLastName(name)
                            .setSalary((double) (this.threadIndex))
            );
            spaceStat.gigaSpace.write(
                    new Organization()
                            .setId(this.threadIndex)
                            .setName(name)
            );

            //construct PreparedStatement
            final String innerJoinQuery =
                    "SELECT * " +
                    "FROM " + Person.class.getName() + " " +
                    "WHERE salary >= ? AND salary <= ?";
            //connection not thread safe, use synchronized to get prepareStatement.
            synchronized (spaceStat.mutex) {
                this.preparedStatement = spaceStat.connection.prepareStatement(innerJoinQuery);
            }
            double rangeFactor = 0.5d;
            this.minSalary = this.threadIndex - rangeFactor;
            this.maxSalary = this.threadIndex + rangeFactor;
            this.preparedStatement.setDouble(1, this.minSalary);
            this.preparedStatement.setDouble(2, this.maxSalary);
        }

        @TearDown
        public void tearDown() throws SQLException {
            if(this.preparedStatement != null) {
                this.preparedStatement.close();
            }
        }

        public PreparedStatement getStatement() {
            return this.preparedStatement;
        }

        public boolean validateResults(ResultSet resultSet) throws Exception {
            if (this.enableValidation) {
                // resultSet field order/index:
                // | p.firstName | p.id | p.lastName | p.organizationId | p.salary |
                // |      1      |   2  |     3      |        4         |    5     |
                ArrayList<Person> people = new ArrayList<>();
                while (resultSet.next()) {
                    Person p = new Person()
                            .setFirstName(resultSet.getString(1))
                            .setId(resultSet.getInt(2))
                            .setLastName(resultSet.getString(3))
                            .setOrganizationId(resultSet.getInt(4))
                            .setSalary(resultSet.getDouble(5));
                    people.add(p);
                }
                if(people.size() > 0){
                    Assert.assertEquals("results length should be 1", 1, people.size());
                    for (Person person : people){
                        Assert.assertTrue("wrong result returned",
                                this.minSalary <= person.getSalary() && person.getSalary() <= this.maxSalary);
                    }
                    return true;
                } else {
                    throw new Exception("results of thread [" + this.threadIndex + "] are empty");
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws RunnerException, SQLException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryAndBenchmark.class.getName())
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