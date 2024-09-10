package org.rudi.microservice.selfdata.service.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class MonoUtils {

	private MonoUtils() {
	}

	/**
	 * @param mono                  renvoie un résultat dans le cas nominal mais peut lancer une exception Reactor en cas d'erreur
	 * @param exceptionEncapsulator fonction qui encapsule l'exception Reactor
	 * @param <T>                   type de résultat du mono
	 * @param <E>                   type de l'exception à lancer
	 * @return le résultat du mono si aucune erreur
	 * @throws E l'exception créée en encapsulant l'exception Reactor en cas d'erreur avec le mono
	 * @see Mono#doOnError(Consumer)
	 * @see Mono#doOnError(Class, Consumer)
	 * @see Mono#onErrorContinue(Class, BiConsumer)
	 * @see Mono#onErrorResume(Function)
	 */
	public static <T, E extends Exception> T blockOrThrow(Mono<T> mono, Function<Throwable, E> exceptionEncapsulator)
			throws E {
		return blockOrCatchAndThrow(mono, Throwable.class, exceptionEncapsulator);
	}

	/**
	 * @param mono                  renvoie un résultat dans le cas nominal mais peut lancer une exception Reactor en cas d'erreur
	 * @param exceptionEncapsulator fonction qui encapsule l'exception Reactor
	 * @param <M>                   type de résultat du Mono
	 * @param <T>                   type de l'exception à catcher quand elle est lancée par le Mono
	 * @param <E>                   type de l'exception à lancer
	 * @return le résultat du mono si aucune erreur
	 * @throws E l'exception créée en encapsulant l'exception Reactor en cas d'erreur avec le Mono
	 * @see Mono#doOnError(Consumer)
	 * @see Mono#doOnError(Class, Consumer)
	 * @see Mono#onErrorContinue(Class, BiConsumer)
	 * @see Mono#onErrorResume(Function)
	 */
	public static <M, T, E extends Exception> M blockOrCatchAndThrow(Mono<M> mono, Class<T> throwableToCatchClass,
			Function<T, E> exceptionEncapsulator) throws E {
		try {
			return mono.block();
		} catch (RuntimeException reactorException) {
			final var reactorThrowable = Exceptions.unwrap(reactorException);
			if (throwableToCatchClass.isInstance(reactorThrowable)) {
				throw exceptionEncapsulator.apply(throwableToCatchClass.cast(reactorThrowable));
			} else {
				throw reactorException;
			}
		}
	}

}
