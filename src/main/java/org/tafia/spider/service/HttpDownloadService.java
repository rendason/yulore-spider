package org.tafia.spider.service;

import java.io.File;

/**
 * Created by Dason on 2017/6/16.
 */
public interface HttpDownloadService {

    String asString(String url);

    File asFile(String url);
}
