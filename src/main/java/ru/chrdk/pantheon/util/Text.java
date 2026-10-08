package ru.chrdk.pantheon.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Мелкие текстовые утилиты (даты в сообщениях). */
public final class Text {
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

	private Text() {
	}

	public static String date(long epochMillis) {
		return DATE.format(Instant.ofEpochMilli(epochMillis));
	}
}
