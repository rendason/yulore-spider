package org.tafia.spider;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by Dason on 2017/6/15.
 */
public class AppContext {

    public static InputStream getResource(String name) {
        return AppContext.class.getClassLoader().getResourceAsStream(name);
    }

    public static File getFile(String name) {
        return new File(AppContext.class.getClassLoader().getResource(name).getPath());
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
