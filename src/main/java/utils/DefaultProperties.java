package utils;

public class DefaultProperties {
    public static final int FORKS_DEFAULT = 1;
    public static final int WARMUP_ITERATIONS_DEFAULT = 5;
    public static final int MEASUREMENT_ITERATIONS_DEFAULT = 25;
    public static final String DEFAULT_SPACE_NAME = "test";
    public static final String[] JVM_ARGS_EMBEDDED_DEFAULT = "-Xms1g -Xmx1g -XX:+UseSerialGC".split(" ");

    public static final String PARAM_MODE="mode";
    public static final String MODE_EMBEDDED="embedded";
    public static final String MODE_REMOTE="remote";
    public static final String PARAM_OBJECT_COUNT = "threadObjectCount";
    public static final String DEFAULT_OBJECT_COUNT = "2";
    public static final String SINGLE_OBJECT = "1";
}
