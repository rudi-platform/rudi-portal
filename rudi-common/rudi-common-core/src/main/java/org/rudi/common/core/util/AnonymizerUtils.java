package org.rudi.common.core.util;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnonymizerUtils {

	private static final double VISBILE_RATIO = 0.2d;

	public static String anonymize(String value) {
		if (StringUtils.isEmpty(value)) {
			return "*";
		}
		int visibleLength = Math.max(0, (int) (value.length() * VISBILE_RATIO));
		if (visibleLength > 0) {
			return value.substring(0, visibleLength) + "*";
		} else {
			return "*";
		}
	}

}
