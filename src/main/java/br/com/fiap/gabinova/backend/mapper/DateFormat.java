package br.com.fiap.gabinova.backend.mapper;

import java.time.Instant;

public final class DateFormat {

    private DateFormat() {
    }

    public static String iso(Instant instant) {
        return instant == null ? null : instant.toString();
    }
}
