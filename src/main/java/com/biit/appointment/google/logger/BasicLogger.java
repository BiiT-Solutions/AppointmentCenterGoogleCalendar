
package com.biit.appointment.google.logger;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public abstract class BasicLogger {

    protected BasicLogger() {
        super();
    }

    public static void warning(Logger logger, String messageTemplate, Object... args) {
        logger.warn(messageTemplate, args);
    }

    public static void warning(Logger logger, String className, String messageTemplate, Object... args) {
        logger.warn(className + ": " + messageTemplate, args);
    }

    public static void info(Logger logger, String messageTempplate, Object... args) {
        logger.info(messageTempplate, args);
    }

    public static void info(Logger logger, String className, String messageTemplate, Object... args) {
        info(logger, className + ": " + messageTemplate, args);
    }

    public static void debug(Logger logger, String messageTemplate, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(messageTemplate, args);
        }
    }

    public static void debug(Logger logger, String className, String messageTemplate, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(className + ": " + messageTemplate, args);
        }
    }

    protected static void severe(Logger logger, String messageTemplate, Object... args) {
        logger.error(messageTemplate, args);
    }

    public static void severe(Logger logger, String className, String messageTemplate, Object... args) {
        severe(logger, className + ": " + messageTemplate, args);
    }

    public static void errorMessageNotification(Logger logger, String messageTemplate, Object... args) {
        severe(logger, messageTemplate, args);
    }

    public static void errorMessageNotification(Logger logger, String className, String messageTemplate, Object... args) {
        severe(logger, className, messageTemplate, args);
    }

    public static String getStackTrace(Throwable throwable) {
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
    }
}
