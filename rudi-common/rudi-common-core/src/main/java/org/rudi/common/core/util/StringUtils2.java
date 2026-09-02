/**
 * 
 */
package org.rudi.common.core.util;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author FNI18300
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils2 {

	private static final String STAR = "*";
	public static final String PERCENT = "%";

	public static String normalize(String criteria) {
		String normalized = StringUtils.stripAccents(criteria.trim()).replaceAll("[^-a-zA-Z0-9% ]", "");
		normalized = StringUtils.normalizeSpace(normalized);
		return normalized;
	}

	public static boolean containsWildcard(String criteria) {
		return criteria != null && criteria.indexOf('*') != -1;
	}

	public static String convertWildcard(String value) {
		return value != null ? value.replace(STAR, PERCENT) : value;
	}

	public static String wildcards(String value, boolean sql) {
		if (sql) {
			return PERCENT + value + PERCENT;
		} else {
			return STAR + value + STAR;
		}
	}

	public static String wildcards(String value) {
		return wildcards(value, false);
	}

	public static String leftWildcards(String value, boolean sql) {
		if (sql) {
			return PERCENT + value;
		} else {
			return STAR + value;
		}
	}

	public static String rightWildcards(String value, boolean sql) {
		if (sql) {
			return value + PERCENT;
		} else {
			return value + STAR;
		}
	}

	public static String leftWildcards(String value) {
		return leftWildcards(value, false);
	}

	public static String rightWildcards(String value) {
		return rightWildcards(value, false);
	}
}
