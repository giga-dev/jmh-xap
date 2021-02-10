package jmh.benchmarks.sql;

import com.j_spaces.jdbc.driver.GConnection;
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
//            System.out.println(((GResultSet) resultSet).getResult().toString());
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

//                System.out.println(p);
                people.add(p);

                Organization o = new Organization()
                        .setId(resultSet.getInt(6))
                        .setName(resultSet.getString(7));

//                System.out.println(o);
                bh.consume(o);
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
        public void setup() throws SQLException {
            gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));
            gigaSpace.clear(null);
            try {
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
        private double minSalary;
        private double maxSalary;

        @Setup
        public void setup(SpaceState spaceStat, ThreadParams threadParams) throws SQLException {
            int threadIndex = threadParams.getThreadIndex();
            String name = String.valueOf(threadIndex);
            //write Person object
            spaceStat.gigaSpace.write(
                    new Person()
                            .setId(threadIndex)
                            .setOrganizationId(threadIndex)
                            .setFirstName(name)
                            .setLastName(name)
                            .setSalary((double) (threadIndex))
            );
            //write Organization object
            spaceStat.gigaSpace.write(
                    new Organization()
                            .setId(threadIndex)
                            .setName(name)
            );

            //construct PreparedStatement
            try {
                final String innerJoinQuery =
                        "SELECT *, count(*) " +
                        "FROM " + Person.class.getName() + " As p " +
                        "INNER JOIN " + Organization.class.getName() + " As o " +
                        "ON p.id = o.id " +
                        "WHERE p.salary >= ? AND p.salary <= ?";


                //connection not thread safe, use synchronized to get prepareStatement.
                synchronized (spaceStat.mutex) {
                    this.preparedStatement = spaceStat.connection.prepareStatement(innerJoinQuery);
                }
                double rangeFactor = 0.5d;
                this.minSalary = threadIndex - rangeFactor;
                this.maxSalary = threadIndex + rangeFactor;
                this.preparedStatement.setDouble(1, this.minSalary);
                this.preparedStatement.setDouble(2, this.maxSalary);

            } catch (SQLException e) {
                this.preparedStatement.close();
                throw e;
            }

        }

        @TearDown
        public void tearDown() throws SQLException {
            this.preparedStatement.close();
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
                            Assert.assertTrue("Wrong result returned!",
                                    this.minSalary <= person.getSalary() && person.getSalary() <= this.maxSalary);
                        } else {
                            throw new Exception("person can't be null!");
                        }
                    }
                    return true;
                } else {
                    throw new Exception("people can't be null or empty!");
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
                .param(PARAM_SQL_ENABLE_VALIDATION, SQL_ENABLE_VALIDATION)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();

    }
}