package org.rudi.common.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"org.rudi.common.core",
		"org.rudi.common.service",
		"org.rudi.common.storage"
})
public class SpringBootTestAppplication {
}
