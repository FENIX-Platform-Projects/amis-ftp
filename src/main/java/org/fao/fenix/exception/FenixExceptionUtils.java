package org.fao.fenix.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class FenixExceptionUtils {

    public static String exceptionStackTraceToString(Exception e){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        return baos.toString();
    }
}
