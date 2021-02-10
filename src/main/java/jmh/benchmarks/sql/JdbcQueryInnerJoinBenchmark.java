package jmh.benchmarks.sql;

import com.j_spaces.jdbc.driver.GConnection;
import com.j_spaces.jdbc.driver.GResultSet;
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
import java.sql.*;
import java.util.ArrayList;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class JdbcQueryInnerJoinBenchmark {

    @Benchmark
    public void testJdbcQueryInnerJoin(ThreadState threadState, Blackhole bh) throws Exception {
        try (ResultSet resultSet = threadState.getStatement().executeQuery()) {
            System.out.println(((GResultSet) resultSet).getResult().toString());
            // resultSet field order/index:
            // | p.firstName | p.id | p.lastName | p.organizationId | p.salary | o.id | o.name |
            // |      1      |   2  |     3      |        4         |    5     |   6  |   7    |
            ArrayList<Person> people = new ArrayList<>();
            while (resultSet.next()) {
                Person p = new Person()
                        .setFirstName(resultSet.getString(1))
                        .setId(resultSet.getInt(2))
                        .setLastName(resultSet.getString(3))
                        .setOrganizationId(resultSet.getInt(4))
                        .setSalary(resultSet.getDouble(5));
                people.add(p);

//                Organization o = new Organization()
//                        .setId(resultSet.getInt(6))
//                        .setName(resultSet.getString(7));
//                bh.consume(o);
            }
            bh.consume(threadState.validateResults(people));
            bh.consume(people);
        }
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;

        private GigaSpace gigaSpace;
        private Connection connection;
        private final Object mutex = new Object(); // mutex for 'connection' because its not thread safe.

        @Setup
        public void setup(ThreadParams threadParams) throws SQLException {
            gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));
            gigaSpace.clear(null);
            try {
                System.out.println(" >>> Hey! I'm in Space setup!, my id - "   + threadParams.getThreadIndex());
                this.connection = GConnection.getInstance(this.gigaSpace.getSpace());
            } catch (SQLException e) {
                this.connection.close();
                throw e;
            }
        }

        @TearDown
        public void teardown() throws SQLException {
            this.connection.close();

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

        @Param({"false"})
        private boolean enableValidation;

        private PreparedStatement preparedStatement;
        private Connection connection;

        private double minSalary;
        private double maxSalary;
        private int threadIndex;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) throws SQLException {
            this.connection = GConnection.getInstance(spaceStat.gigaSpace.getSpace());
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
            try {
                final String innerJoinQuery =
//                        "SELECT * " +
//                        "FROM " + Person.class.getName() + " As p, " + Organization.class.getName() + " As o " +
//                        "WHERE p.id = o.id AND p.salary >= " + (threadIndex - 0.5d) + " AND p.salary <= " + (threadIndex + 0.5d) ;

                        "EXPLAIN PLAN FOR SELECT * " +
                        "FROM " + Person.class.getName() + " As p " +
                        "INNER JOIN " + Organization.class.getName() + " As o " +
                        "ON p.id = o.id " +
                        "WHERE p.salary >= " + (this.threadIndex - 0.5d) + " AND p.salary <= " + (this.threadIndex + 0.5d);

                //connection not thread safe, use synchronized to get prepareStatement.
//                synchronized (spaceStat.mutex) {
//                    System.out.println(spaceStat.mutex + " " + threadIndex);
//                    this.preparedStatement = spaceStat.connection.prepareStatement(innerJoinQuery);
//                }

                this.preparedStatement = this.connection.prepareStatement(innerJoinQuery);
                double rangeFactor = 0.5d;
                this.minSalary = this.threadIndex - rangeFactor;
                this.maxSalary = this.threadIndex + rangeFactor;
//                this.preparedStatement.setDouble(1, this.minSalary);
//                this.preparedStatement.setDouble(2, this.maxSalary);
                System.out.println(this.preparedStatement + " ps for id " + this.threadIndex);
                System.out.println(this.connection + " con for id " + this.threadIndex);

            } catch (SQLException e) {
                System.out.println("Here!!! " + this.threadIndex);
                this.preparedStatement.close();
                throw e;
            }
        }

        @TearDown
        public void tearDown() throws SQLException {
            this.preparedStatement.close();
            this.connection.close();
        }

        public PreparedStatement getStatement() {
            return this.preparedStatement;
        }

        public boolean validateResults(ArrayList<Person> people) throws Exception {
            if (enableValidation) {
                if(people != null && people.size() > 0){
                    Assert.assertEquals("results length should be 1", 1, people.size());
                    for (Person person : people){
                        if (person != null){
                            Assert.assertTrue("wrong result returned",
                                    this.minSalary <= person.getSalary() && person.getSalary() <= this.maxSalary);
                        } else {
                            throw new Exception("result of thread [" + this.threadIndex + "] are null");
                        }
                    }
                    return true;
                } else {
                    throw new Exception("results of thread [" + this.threadIndex + "] are null or empty!");
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws RunnerException, SQLException {
        Options opt = new OptionsBuilder()
                .include(JdbcQueryInnerJoinBenchmark.class.getName())
                .param(PARAM_MODE, MODE_REMOTE)
                .warmupIterations(1).warmupTime(TimeValue.seconds(1))
                .measurementIterations(1).measurementTime(TimeValue.seconds(1))
//                .param(PARAM_SQL_ENABLE_VALIDATION, SQL_ENABLE_VALIDATION)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}