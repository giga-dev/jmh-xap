package utils;

import org.junit.Assert;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.util.Statistics;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;
import static utils.FormatUtils.df;

public class Assertions {

    public static void assertResults(BaselineStatistics baseStatistics, Collection<RunResult> runResults) {
        assertFalse("result set is empty", runResults.isEmpty());
        for(RunResult runResult : runResults) {
            Assertions.assertResults(baseStatistics, runResult.getPrimaryResult().getStatistics());
        }
    }

    public static void assertResults(Collection<RunResult> firstResult, Collection<RunResult> secondResult) {
        assertFalse("first result set is empty", firstResult.isEmpty());
        assertFalse("second result set is empty", secondResult.isEmpty());
        assertEquals("results have different size", firstResult.size(), secondResult.size());
        Iterator<RunResult> firstIterator = firstResult.iterator();
        Iterator<RunResult> secondIterator = secondResult.iterator();
        while (firstIterator.hasNext() && secondIterator.hasNext()) {
            assertCompareResults(firstIterator.next(), secondIterator.next(), 0.05);
        }
    }

    /** assert compare results at a predefined Confidence Interval (CI) of 99.9% */
    private static void assertCompareResults(RunResult first, RunResult second, double maxDeviation) {

        final double CONFIDENCE_INTERVAL = 0.999;

        Statistics firstStatistics = first.getPrimaryResult().getStatistics();
        String firstBenchmarkName = first.getParams().getBenchmark().substring(first.getParams().getBenchmark().lastIndexOf(".") + 1);
        Statistics secondStatistics = second.getPrimaryResult().getStatistics();
        String secondBenchmarkName = second.getParams().getBenchmark().substring(second.getParams().getBenchmark().lastIndexOf(".") + 1);

        System.out.println("Assert results:");
        System.out.println(firstBenchmarkName + " statistics: \n" + new BaselineStatistics(firstStatistics) +"\n");
        System.out.println(secondBenchmarkName + " statistics: \n" + new BaselineStatistics(secondStatistics) +"\n");

        //make sure we are comparing on same iteration size - default assumes measurementIterations=25
        assertEquals("measurement iterations differ", firstStatistics.getN(), secondStatistics.getN());


        final double[] baseCI = firstStatistics.getConfidenceIntervalAt(CONFIDENCE_INTERVAL);
        final double[] currCI = secondStatistics.getConfidenceIntervalAt(CONFIDENCE_INTERVAL);

        StringBuilder comparisonResultsMsg = new StringBuilder();
        comparisonResultsMsg.append("Throughput comparison results (maximum allowed deviation " + maxDeviation * 100 + "%):").append(System.lineSeparator());
        comparisonResultsMsg.append("Avg :");
        boolean deviatedFromMean = compareResults(
                firstStatistics.getMean(), secondStatistics.getMean(), maxDeviation,  firstBenchmarkName, secondBenchmarkName, comparisonResultsMsg);

        comparisonResultsMsg.append("CI_0:");
        boolean deviatedFromCI_0 = compareResults(baseCI[0], currCI[0], maxDeviation, firstBenchmarkName, secondBenchmarkName, comparisonResultsMsg);

        comparisonResultsMsg.append("CI_1:");
        boolean deviatedFromCI_1 = compareResults(baseCI[1], currCI[1], maxDeviation, firstBenchmarkName, secondBenchmarkName, comparisonResultsMsg);

        final boolean deviatedAll = deviatedFromMean && deviatedFromCI_0 && deviatedFromCI_1;
        if (deviatedAll) {
            comparisonResultsMsg.append("[x] Mean difference is statistically significant");
            System.out.println(comparisonResultsMsg.toString());
             Assert.fail("Deviation from mean is statistically significant");
        } else if (deviatedFromMean || deviatedFromCI_0 || deviatedFromCI_1) {
            comparisonResultsMsg.append("[-] Mean difference is not statistically significant");
            System.out.println(comparisonResultsMsg.toString());
        } else {
            comparisonResultsMsg.append("[+] Results are within the allowed deviation");
            System.out.println(comparisonResultsMsg.toString());
        }
    }

    /** Calculates the ratio of results (relative to minScore) and validate its in the maxDeviation allowed (e.g. 0.05) */
    private static boolean compareResults(double firstScore, double secondScore, double maxDeviation, String firstName, String secondName, StringBuilder msg) {
        double maxScore = Math.max(firstScore, secondScore);
        double minScore = Math.min(firstScore, secondScore);
        String maxScoreName = firstScore > secondScore ? firstName : secondName;
        String minScoreName = firstScore < secondScore ? firstName : secondName;
        msg.append(" " + maxScoreName + " > " + minScoreName);
        return isDeviatedFromScore(minScore, maxScore, maxDeviation, msg);
    }

    /** assert results at a predefined Confidence Interval (CI) of 99.9% */
    private static void assertResults(BaselineStatistics baseStatistics, Statistics currStatistics) {

        final double MAX_DEVIATION = 0.05;
        final double CONFIDENCE_INTERVAL = 0.999;

        System.out.println("Assert results:");
        System.out.println("Baseline statistics: \n" + baseStatistics +"\n");
        System.out.println("Current  statistics: \n" + new BaselineStatistics(currStatistics) +"\n");

        //make sure we are comparing on same iteration size - default assumes measurementIterations=25
        assertEquals("measurement iterations differ", baseStatistics.getN(), currStatistics.getN());

        final double[] baseCI = baseStatistics.getConfidenceIntervalAt(CONFIDENCE_INTERVAL);
        final double[] currCI = currStatistics.getConfidenceIntervalAt(CONFIDENCE_INTERVAL);

        StringBuilder comparisonResultsMsg = new StringBuilder();
        comparisonResultsMsg.append("Throughput comparison results relative to baseline (maximum allowed deviation " + MAX_DEVIATION * 100 + "%):").append(System.lineSeparator());
        comparisonResultsMsg.append("Avg :");
        boolean deviatedFromMean = isDeviatedFromScore(
                baseStatistics.getMean(), currStatistics.getMean(), MAX_DEVIATION, comparisonResultsMsg);

        comparisonResultsMsg.append("CI_0:");
        boolean deviatedFromCI_0 = isDeviatedFromScore(baseCI[0], currCI[0], MAX_DEVIATION, comparisonResultsMsg);

        comparisonResultsMsg.append("CI_1:");
        boolean deviatedFromCI_1 = isDeviatedFromScore(baseCI[1], currCI[1], MAX_DEVIATION, comparisonResultsMsg);

        final boolean deviatedAll = deviatedFromMean && deviatedFromCI_0 && deviatedFromCI_1;
        if (deviatedAll) {
            comparisonResultsMsg.append("[x] Mean difference is statistically significant");
            System.out.println(comparisonResultsMsg.toString());
            Assert.fail("Deviation from mean is statistically significant");
        } else if (deviatedFromMean || deviatedFromCI_0 || deviatedFromCI_1) {
            comparisonResultsMsg.append("[-] Mean difference is not statistically significant");
            System.out.println(comparisonResultsMsg.toString());
        } else {
            comparisonResultsMsg.append("[+] Results are within the allowed deviation");
            System.out.println(comparisonResultsMsg.toString());
        }
    }


    /** Calculate if current score deviated from baseline score by maxDeviation (e.g. 0.05) */
    private static boolean isDeviatedFromScore(double base, double curr, double maxDeviation, StringBuilder msg) {
        final double deviation = Math.abs(curr/base - 1);
        final boolean exceeds = maxDeviation < deviation;
        final String deviationString = df.format(deviation * 100) + "%";
        msg.append(" ("+df.format(curr)+"/"+df.format(base)+") = " + deviationString);
        if (exceeds) {
            String maxDeviationString = df.format(maxDeviation * 100) + "%";
            msg.append(" - exceeds " + maxDeviationString);
        }
        msg.append(System.lineSeparator());
        return exceeds;
    }
}
