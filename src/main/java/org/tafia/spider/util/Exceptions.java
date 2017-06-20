package org.tafia.spider.util;

/**
 * Created by Dason on 2017/6/20.
 */
public class Exceptions {

    private Exceptions() {
    }

    public static RuntimeException asUnchecked(Exception e) {
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }
}
