package org.tafia.spider.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tafia.spider.service.HttpDownloadService;
import org.tafia.spider.util.Exceptions;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Http下载服务实现类
 */
@Service
public class HttpDownloadServiceImpl implements HttpDownloadService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private CloseableHttpClient httpClient;

    private File tempDir = new File(System.getProperty("java.io.tmpdir"), "yulore");

    @PostConstruct
    public void init() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(16);
        httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
        logger.info("Initialized http client with {} connection pool.", connectionManager.getMaxTotal());
        if (!(tempDir.exists() || tempDir.mkdirs())) {
            throw new IllegalArgumentException("Can not create temp dir : " + tempDir.getAbsolutePath());
        }
        logger.info("Initialized temporary directory : {}", tempDir.getAbsolutePath());
    }

    @Override
    public String asString(String url) {
        if (!check(url)) return null;
        try (CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(url))) {
            if (httpResponse.getStatusLine().getStatusCode() != 200) return null;
            return IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw Exceptions.asUnchecked(e);
        }
    }

    @Override
    public File asFile(String url) {
        if (!check(url)) return null;
        try (CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(url))) {
            if (httpResponse.getStatusLine().getStatusCode() != 200) return null;
            File file = new File(tempDir, getFileName(url));
            if (!(file.exists() || file.createNewFile()))
                throw new AccessDeniedException("Cannot create file : " + file.getAbsolutePath());
            InputStream inputStream = httpResponse.getEntity().getContent();
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
            }
            return file;
        } catch (IOException e) {
            throw Exceptions.asUnchecked(e);
        }
    }

    private boolean check(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private String getFileName(String url) {
        int index = url.indexOf('?');
        if (index != -1) url = url.substring(0, index);
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = formatter.format(LocalDateTime.now());
        index = fileName.lastIndexOf('.');
        if (index == -1) return fileName + "-" + timestamp;
        return fileName.substring(0, index) + "-" + timestamp + fileName.substring(index);

    }

    @PreDestroy
    public void destroy() {
        try {
            httpClient.close();
        } catch (IOException e) {

        }
    }
}
