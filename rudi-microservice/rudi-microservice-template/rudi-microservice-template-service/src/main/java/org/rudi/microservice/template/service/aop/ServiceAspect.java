package org.rudi.microservice.template.service.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ServiceAspect {

	@Pointcut("execution(* org.rudi.microservice.template.service.*.*.impl.*.*(..))")
	public void businessMethods() {
		// Methode de déclaration des points de coupure
	}

	@Around("businessMethods()")
	public Object profile(final ProceedingJoinPoint pjp) throws Throwable {
		final long start = System.currentTimeMillis();
		final Object output = pjp.proceed();
		final long elapsedTime = System.currentTimeMillis() - start;
		log.info("{} - {} - {}", elapsedTime, pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName());
		return output;
	}

}
