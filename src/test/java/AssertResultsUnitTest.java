import org.junit.Test;
import jmh.utils.Assertions;
import jmh.utils.BaselineStatistics;

/** Tests that Assertions.assertResults returns as expected */
public class AssertResultsUnitTest {

    @Test
    public void test1() {
        BaselineStatistics base
                = new BaselineStatistics(1936645.252, 2001733.752, 2059101.215, 34104.486);
        BaselineStatistics curr
                = new BaselineStatistics(2058431.994,2106494.604,2167382.014,42690.893);
        Assertions.assertResults(base, curr);
    }

    @Test
    public void test2() {
        BaselineStatistics base
                = new BaselineStatistics(17023.266, 17553.047, 17923.291, 231.504);
        BaselineStatistics curr
                = new BaselineStatistics(16887.836, 17219.557, 17450.840, 170.020);
        Assertions.assertResults(base, curr);
    }

    @Test
    public void test3() {
        BaselineStatistics base
                = new BaselineStatistics(43506.679, 46463.806,47466.763,964.645);
        BaselineStatistics curr
                = new BaselineStatistics(47533.893,48055.620,48595.296,349.474);
        Assertions.assertResults(base, curr);
    }

    @Test
    public void test4() {
        BaselineStatistics base
                = new BaselineStatistics(16131.971,17616.408,17886.991,393.313);
        BaselineStatistics curr
                = new BaselineStatistics(17574.622,18084.712,18497.789,289.090);
        Assertions.assertResults(base, curr);
    }

    @Test
    public void test5() {
        BaselineStatistics base
                = new BaselineStatistics(17023.266,17553.047,17923.291,231.504);
        BaselineStatistics curr
                = new BaselineStatistics(16887.836,17219.557,17450.840,170.020);
        Assertions.assertResults(base, curr);
    }

    @Test
    public void test6() {
        BaselineStatistics base
                = new BaselineStatistics(46998.906,48995.602,49876.066,625.967);
        BaselineStatistics curr
                = new BaselineStatistics(43121.079,46481.395,48062.498,1624.184);
        Assertions.assertResults(base, curr);
    }

    @Test //failed
    public void test7() {
        BaselineStatistics base
                = new BaselineStatistics(18633.285,19002.748,19332.972,177.494);
        BaselineStatistics curr
                = new BaselineStatistics(15752.091,18260.940,18724.081,579.178);
        Assertions.assertResults(base, curr);
    }
}
