package org.tafia.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Created by Dason on 2017/6/15.
 */
public class OfflineDataParser {
    
    private static final File BASE_DIR = new File("F:\\\\dason\\dhb-origin");

    private static final File TARGET_DIR = new File("F:\\\\dason\\dhb-data");

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(16);
        getRootCatagoryList().stream()
                .map(OfflineDataParser::getTask)
                .forEach(threadPool::execute);
        threadPool.shutdown();
    }

    private static List<String> getRootCatagoryList() {
        String[] names = new File("F:\\\\dason\\dhb-origin").list((dir1, name) -> name.startsWith("c"));
        if (names == null) return Collections.emptyList();
        return Stream.of(names)
                .map(name -> name.substring(1, name.length() - 5))
                .collect(Collectors.toList());
    }

    private static Runnable getTask(String categoryId) {
        return () -> {
            File datFile = new File(BASE_DIR, "d" + categoryId + ".dat");
            System.out.println("Parsing " + datFile.getName());
            File targetFile = new File(TARGET_DIR, "d" + categoryId + ".json");
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(datFile, "r");
                BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, true))) {
                List<int[]> indexList = getIndexList(categoryId);
                for (int[] ints : indexList) {
                    try {
                        String json = getDataString(randomAccessFile, ints[0], ints[1]);
                        writer.write(json);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Parsed " + datFile.getName());
        };
    }

    private static String decompress(byte[] bytes) throws IOException {
        try (InputStream headerInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return IOUtils.toString(headerInputStream, StandardCharsets.UTF_8);
        }
    }

    private static byte[] read(InputStream inputStream, int size) throws IOException {
        byte[] buffer = new byte[size];
        int length = inputStream.read(buffer, 0, size);
        return fit(buffer, length);
    }

    private static byte[] read(RandomAccessFile randomAccessFile, int offset, int size) throws IOException {
        randomAccessFile.seek(offset);
        byte[] buffer = new byte[size];
        int length = randomAccessFile.read(buffer, 0, size);
        if (length == -1) return null;
        return fit(buffer, length);
    }

    private static byte[] fit(byte[] bytes, int size) {
        if (size == bytes.length) return bytes;
        byte[] result = new byte[size];
        System.arraycopy(bytes, 0, result, 0, size);
        return result;
    }

    private static int validLength(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if ((bytes[i] != 0) && (i > 1) && ((bytes[(i - 2)] | bytes[(i - 1)]) == 0)) {
                return i;
            }
        }
        return bytes.length;
    }

    private static byte[] mask(byte[] bytes, int arrayLength, int maskLength) {
        final byte[] mask = {31, -117, 8, 0, 0, 0, 0, 0, 0, 3};
        byte[] result = new byte[arrayLength + maskLength];
        System.arraycopy(mask, 0, result, 0, maskLength);
        System.arraycopy(bytes, 0, result, maskLength, arrayLength);
        return result;
    }

    private static List<Integer> getOffsetList(JSONObject jsonObject) {
        return jsonObject.keySet().stream().map(jsonObject::getInteger).collect(Collectors.toList());
    }

    private static List<int[]> getIndexList(String categoryId) {
        try {
            String icFileName = "d" + categoryId + "_ic.dat";
            File icFile = new File(BASE_DIR, icFileName);
            //loading header
            byte[] bytes;
            try (InputStream inputStream = new FileInputStream(icFile)) {
                bytes = read(inputStream, 4096);
            }
            int headerLength = validLength(bytes);
            bytes = mask(bytes, headerLength, 4);
            String headerString = decompress(bytes);
            JSONObject header = JSON.parseObject(headerString);
            //loaded header
            List<Integer> offsetList = getOffsetList(header);
            List<int[]> result = new ArrayList<>();
            try (RandomAccessFile raFile = new RandomAccessFile(icFile, "r")) {
                offsetList.forEach(offset -> {
                    try {
                        byte[] buffer = read(raFile, headerLength + offset, 2048);
                        if (buffer == null) return;
                        int indexLength = validLength(buffer);
                        buffer = mask(buffer, indexLength, 4);
                        String searchItemsString = decompress(buffer);
                        JSONArray jsonArray = JSON.parseArray(searchItemsString);
                        int[] index = IntStream.range(0, jsonArray.size()).map(jsonArray::getIntValue).toArray();
                        for (int i = 0; i < index.length - 1; i += 2) {
                            result.add(new int[]{index[i], index[i + 1]});
                        }
                    } catch (EOFException ignore) {

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static String getDataString(RandomAccessFile randomAccessFile, int offset, int length) throws IOException {
        byte[] bytes = read(randomAccessFile, offset, length);
        bytes = mask(bytes, bytes.length, 8);
        return decompress(bytes);
    }
}
