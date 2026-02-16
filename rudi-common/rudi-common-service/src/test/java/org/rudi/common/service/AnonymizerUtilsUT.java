/**
 * RUDI Portail
 */
package org.rudi.common.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.rudi.common.core.util.AnonymizerUtils;

/**
 * 
 */
@CommonServiceSpringBootTest
class AnonymizerUtilsUT {

	private static final List<String> VALUES = List.of("", "1", "12", "123", "1234", "12345", "123456", "1234567",
			"12345678", "12345679", "123456790", "1234567901", UUID.randomUUID().toString());

	@Test
	void testAnonymizer() {
		for (String value : VALUES) {
			String result = AnonymizerUtils.anonymize(value);
			assertNotEquals(result, value);
		}
	}
}
