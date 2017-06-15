package org.tafia.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App {

    private static final File BASE_PATH = new File("F:\\\\yulore");

    private static CloseableHttpClient httpClient = HttpClients.createDefault();

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    public static void main( String[] args ) {
        try {
            List<String> urls = getPackageUrls();
            System.out.println("Loaded " + urls.size() + " urls");
            urls.parallelStream().forEach(App::download);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> getPackageUrls() throws IOException {
        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("cities.json")) {
            JSONObject jsonObject = JSON.parseObject(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            JSONArray cities = jsonObject.getJSONArray("cities");
            return cities.stream().map(e -> (JSONObject) e)
                    .map(e -> e.getString("pkg"))
                    .collect(Collectors.toList());
        }
    }

    private static void download(String url) {
        HttpGet httpGet = new HttpGet(url);
        System.out.println("[" + COUNTER.getAndIncrement() + "] Downloading " + url);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            InputStream inputStream = httpResponse.getEntity().getContent();
            save(getFileName(url), inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save(String name, InputStream inputStream) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(new File(BASE_PATH, name))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer, 0, 1024)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        }
    }

    private static String getFileName(String url) {
        return url.substring(24, url.indexOf('?'));
    }
}
