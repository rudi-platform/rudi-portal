package org.rudi.common.service.datafactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class AbstractDataFactory {

	protected final static Random rand = new Random();

	protected String randomString(int length) {
		int leftLimit = 33;
		int rightLimit = 126;
		StringBuilder buffer = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int randomLimitedInt = leftLimit + (int)
					(rand.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		return buffer.toString();
	}

	/**
	 * @param date -
	 * @return si date non null : la date troncé au micro secondes, sinon null
	 */
	public LocalDateTime handleDate(LocalDateTime date) {
		if(date != null) {
			date = date.truncatedTo(ChronoUnit.MICROS);
		}
		return date;
	}
}
