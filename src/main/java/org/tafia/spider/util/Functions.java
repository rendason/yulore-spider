package org.tafia.spider.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Dason on 2017/6/20.
 */
public class Functions {

    private static Logger LOGGER = LoggerFactory.getLogger(Functions.class);

    private Functions() {
    }

    public static <T> Predicate<T> falseOnException(Predicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                LOGGER.warn("Predicate exception", e);
                return false;
            }
        };
    }

    public static <T, R> Function<T, R> nullOnException(Function<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                LOGGER.warn("Function exception", e);
                return null;
            }
        };
    }
}
