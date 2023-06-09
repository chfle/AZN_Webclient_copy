package com.lokcenter.AZN.helper;

/**
 * Junit Helper
 *
 * Checks where this code is executed
 *
 * @version 1.2 2022-07-30
 */
public class JunitHelper {
    /**
     * Checks current environment
     *
     * @return boolean
     */
    public static boolean isJUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
}
