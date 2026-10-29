package com.civicfix.platform.common.api;

import org.slf4j.MDC;

/**
 * Thread-local context accessor for the current request's correlation/request ID.
 */
public final class CorrelationIdContext {

    public static final String MDC_CORRELATION_KEY = "correlationId";
    public static final String MDC_REQUEST_KEY = "requestId";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";

    private static final ThreadLocal<String> CURRENT_ID = new ThreadLocal<>();

    private CorrelationIdContext() {
    }

    public static String get() {
        String id = CURRENT_ID.get();
        if (id == null) {
            id = MDC.get(MDC_CORRELATION_KEY);
        }
        return id;
    }

    public static void set(String correlationId) {
        if (correlationId != null) {
            CURRENT_ID.set(correlationId);
            MDC.put(MDC_CORRELATION_KEY, correlationId);
            MDC.put(MDC_REQUEST_KEY, correlationId);
        } else {
            clear();
        }
    }

    public static void clear() {
        CURRENT_ID.remove();
        MDC.remove(MDC_CORRELATION_KEY);
        MDC.remove(MDC_REQUEST_KEY);
    }
}
