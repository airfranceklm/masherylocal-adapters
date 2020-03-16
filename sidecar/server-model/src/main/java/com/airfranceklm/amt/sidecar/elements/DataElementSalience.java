package com.airfranceklm.amt.sidecar.elements;

/**
 * Class defining the salience for data element processing.
 */
public class DataElementSalience {
    public static final int MANDATORY_OPERATIONS = 0;
    public static final int OBVIOUS_ERRORS = 500;
    public static final int DATA_STRUCTS = 900;
    public static final int FAIL_FAST = 1000;
    public static final int CONTENT_CHECK = 2000;
    public static final int CONTENT_OPERATION = 3000;
}
