package utils;

import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriter;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriterSettings;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class CsvUtils {

    /** plots the raw data of the result to csv file (overwrites existing file) */
    public static void toFile(Collection<RunResult> runResults, String filename) {
        CsvWriter csvWriter = new CsvWriter(new File(filename),
                new CsvWriterSettings());

        for (RunResult runResult : runResults) {
            Result primaryResult = runResult.getPrimaryResult();
            csvWriter.writeHeaders(runResult.getParams().getMode().longLabel());
            Iterator<Map.Entry<Double, Long>> rawData = primaryResult.getStatistics().getRawData();
            while (rawData.hasNext()) {
                Map.Entry<Double, Long> next = rawData.next();
                csvWriter.addValue(next.getKey());
                csvWriter.writeValuesToRow();
            }
        }

        csvWriter.flush();
        csvWriter.close();
    }
}
