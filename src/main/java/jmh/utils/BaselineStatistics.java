package jmh.utils;

import org.openjdk.jmh.util.ListStatistics;
import org.openjdk.jmh.util.Statistics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static jmh.utils.DefaultProperties.MEASUREMENT_ITERATIONS_DEFAULT;
import static jmh.utils.FormatUtils.df;

public class BaselineStatistics extends ListStatistics {
    private long N;
    private double max;
    private double min;
    private double mean;
    private double variance;

    public BaselineStatistics(double min, double avg, double max, double stdev) {
        this(MEASUREMENT_ITERATIONS_DEFAULT, min, avg, max, stdev);
    }

    public BaselineStatistics(long N, double min, double avg, double max, double stdev) {
        super();
        this.N = N;
        this.min = min;
        this.mean = avg;
        this.max = max;
        this.variance = Math.pow(stdev, 2);
    }

    public BaselineStatistics(Statistics statistics) {
        this(statistics.getN(), statistics.getMin(), statistics.getMean(), statistics.getMax(), statistics.getStandardDeviation());
    }

    @Override public long getN() { return N;}
    @Override public double getMax() { return max; }
    @Override public double getMin() { return min;}
    @Override public double getMean() { return mean; }
    @Override public double getVariance() { return variance; }

    //helper method since original returns an iterator
    public static double[] getRawDataAsArray(Statistics statistics) {
        double[] data = new double[(int)statistics.getN()];
        Iterator<Map.Entry<Double, Long>> iterator = statistics.getRawData();
        int i=0;
        while (iterator.hasNext()) {
            data[i++] = iterator.next().getKey();
        }
        return data;
    }

    @Override
    public String toString() {
        final double[] interval = getConfidenceIntervalAt(0.999);
        return String.format(" (min, avg, max) = (%s, %s, %s), stdev = %s%n CI (99.9%%): [%s, %s]"
                ,df.format(min), df.format(mean), df.format(max), df.format(getStandardDeviation())
                        ,df.format(interval[0]), df.format(interval[1]));
    }

    public static void main(String[] args) {
        BaselineStatistics baselineStatistics
                = new BaselineStatistics(46998.906, 48995.602,49876.066,625.967);

        System.out.println("baseline: " + baselineStatistics);
        System.out.println("CI (99.9%): " + Arrays.toString(baselineStatistics.getConfidenceIntervalAt(0.999)));
    }
}
