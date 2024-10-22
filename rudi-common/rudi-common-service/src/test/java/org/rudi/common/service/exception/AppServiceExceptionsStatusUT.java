package org.rudi.common.service.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AppServiceExceptionsStatusUT {

	@Test
	void from() {
		assertThat(AppServiceExceptionsStatus.stringValueFrom(HttpStatus.FORBIDDEN))
				.isEqualTo(AppServiceExceptionsStatus.FORBIDDEN.getStringValue());
	}
}
