package com.mygdx.game.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionFormatter {

    public static String formatException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}
