package utils;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.util.Statistics;

import java.util.Collection;

import static utils.FormatUtils.df;

public class Assertions {
    public static void assertFalse(String msg, boolean o) {
        if (o) throw new RuntimeException("assertFalse: " + msg);
    }
    public static void assertTrue(String msg, boolean o) {
        if (!o) throw new RuntimeException("assertTrue: " +msg);
    }

    public static void assertResults(BaselineStatistics baseStatistics, Collection<RunResult> runResults) {
        Assertions.assertFalse("result set is empty", runResults.isEmpty());
        for(RunResult runResult : runResults) {
            Assertions.assertResults(baseStatistics, runResult.getPrimaryResult().getStatistics());
        }
    }

    /** assert results at a predefined confidence level of 99.9% */
    public static void assertResults(BaselineStatistics baseStatistics, Statistics currStatistics) {

        final double MAX_DEVIATION = 0.05;
        System.out.println("Assert results:");
        System.out.println("Baseline statistics: \n" + baseStatistics +"\n");
        System.out.println("Current  statistics: \n" + new BaselineStatistics(currStatistics) +"\n");

        final double[] baseCI = baseStatistics.getConfidenceIntervalAt(0.999);
        final double[] currCI = currStatistics.getConfidenceIntervalAt(0.999);

        boolean deviatedFromMean = isDeviatedFromScore(
                baseStatistics.getMean(), currStatistics.getMean(), MAX_DEVIATION);

        boolean deviatedFromCI_0 = isDeviatedFromScore(baseCI[0], currCI[0], MAX_DEVIATION);

        boolean deviatedFromCI_1 = isDeviatedFromScore(baseCI[1], currCI[1], MAX_DEVIATION);

        final boolean deviatedAll = deviatedFromMean && deviatedFromCI_0 && deviatedFromCI_1;
        if (deviatedAll) {
            System.out.println("[x] Mean difference is statistically significant");
        }

        Assertions.assertFalse ("Deviation from mean is statistically significant", deviatedAll);

        if (deviatedFromMean || deviatedFromCI_0 || deviatedFromCI_1) {
            System.out.println("[-] Mean difference is not statistically significant");
        } else {
            System.out.println("[√] Results are within the allowed deviation of " + (MAX_DEVIATION * 100) + "%");
        }
    }


    /** Calculate if current score deviated from baseline score by maxDeviation (e.g. 0.05) */
    public static boolean isDeviatedFromScore(double base, double curr, double maxDeviation) {
        final double deviation = Math.abs(curr/base - 1);
        final boolean exceeds = maxDeviation < deviation;
        if (exceeds) {
            String deviationString = df.format(deviation * 100) + "%";
            String maxDeviationString = df.format(maxDeviation * 100) + "%";
            System.out.println("("+df.format(curr)+"/"+df.format(base)+") = " + deviationString
                    + " exceeds maximum allowed deviation: " + maxDeviationString);
        }
        return exceeds;
    }
}
