package org.rudi.common.storage.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class StorageAspect {

	@Pointcut("execution(* org.rudi.common.storage.dao.impl.*.*(..))")
	public void businessMethods() {
		// Nothing to do
	}

	@Around("businessMethods()")
	public Object profile(ProceedingJoinPoint pjp) throws Throwable {
		long start = System.currentTimeMillis();
		Object output = pjp.proceed();
		long elapsedTime = System.currentTimeMillis() - start;
		if (log.isInfoEnabled()) {
			log.info("{} - {} {}", elapsedTime, pjp.getSignature().getDeclaringTypeName(),
					pjp.getSignature().getName());
		}
		return output;
	}

}
