package org.rudi.microservice.kalim.service.admin.impl;

public class FetchFailException extends RuntimeException {
	private static final long serialVersionUID = 2424504073123216926L;

	public FetchFailException(Throwable cause) {
		super(cause);
	}
}
