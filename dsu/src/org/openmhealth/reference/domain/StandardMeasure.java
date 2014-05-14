package org.openmhealth.reference.domain;

public class StandardMeasure {
    public static final String DOMAIN = "omh";
    public static final String SCHEMA_PREFIX = "omh:" + DOMAIN + ":";

    public static final String JSON_KEY_EFFECTIVE_TIMEFRAME = 
        "effective-timeframe";
    public static final String JSON_KEY_TIMEFRAME_START_TIME = 
        "start-time";
    public static final String JSON_KEY_TIMEFRAME_END_TIME = 
        "end-time";
    public static final String JSON_KEY_TIMEFRAME_DURATION = 
        "duration";
    public static final String JSON_KEY_DURATION = "duration";
    public static final String JSON_KEY_VALUE = "value";
    public static final String JSON_KEY_UNIT = "unit";
}
