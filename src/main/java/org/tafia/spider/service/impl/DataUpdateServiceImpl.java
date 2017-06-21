package org.tafia.spider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tafia.spider.component.AppContext;
import org.tafia.spider.dao.*;
import org.tafia.spider.model.*;
import org.tafia.spider.service.DataUpdateService;
import org.tafia.spider.service.HttpDownloadService;
import org.tafia.spider.util.Exceptions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

/**
 * Created by Dason on 2017/6/16.
 */
@Service
public class DataUpdateServiceImpl implements DataUpdateService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HttpDownloadService httpDownloadService;

    @Autowired
    private CityDao cityDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private DistrictDao districtDao;

    @Autowired
    private FrequentSearchDao frequentSearchDao;

    @Autowired
    private SubjectDao subjectDao;

    @Transactional
    @Override
    public void update(City city) {
        logger.info("Downloading data of special city : {}", city.getName());
        cityDao.insert(city);
        File zipFile = httpDownloadService.asFile(city.getPackagee());
        File decompressDir = unzip(zipFile);
        File categoryFile = new File(decompressDir, String.format("c%s.json", city.getId()));
        logger.info("Loading category file : " + categoryFile.getAbsolutePath());
        JSONObject content = JSON.parseObject(getText(categoryFile));
        addAllCategories(city, content);
        addAllDistricts(city, content);
        addFrequentSearch(city, content);
        File indexFile = new File(decompressDir, String.format("d%s_ic.dat", city.getId()));
        logger.info("Loading index file : " + indexFile.getAbsolutePath());
        List<DataIndex> dataIndices = getDataIndices(indexFile);
        File dataFile = new File(decompressDir, String.format("d%s.dat", city.getId()));
        logger.info("Loading data file : " + dataFile.getAbsolutePath());
        loadDataFromFile(city, dataFile, dataIndices);
        logger.info("Finished to update data of special city : {}", city.getName());
    }

    private File unzip(File file) {
        try {
            File path = getUnzipPath(file);
            ZipFile zipFile = new ZipFile(file);
            zipFile.stream().forEach(zipEntry -> {
                if (zipEntry.isDirectory()) {
                    File unzipDir = new File(path, zipEntry.getName());
                    if (!(unzipDir.exists() || unzipDir.mkdirs()))
                        throw new RuntimeException("Cannot create directory " + unzipDir);
                } else {
                    File unzipFile = new File(path, zipEntry.getName());
                    try {
                        if (!unzipFile.exists()) unzipFile.createNewFile();
                        try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                             OutputStream outputStream = new FileOutputStream(unzipFile)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = inputStream.read(buffer, 0, 4096)) != -1) {
                                outputStream.write(buffer, 0, len);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return path;
        } catch (Exception e) {
            logger.warn("Exception throws on decompressing", e);
            throw Exceptions.asUnchecked(e);
        }
    }

    private File getUnzipPath(File zipFile) {
        String fileName = zipFile.getName();
        int index = fileName.lastIndexOf('.');
        if (index != -1) fileName = fileName.substring(0, index);
        else fileName += "-unzip";
        File path = new File(zipFile.getParent(), fileName);
        if (!path.exists()) path.mkdirs();
        return path;
    }

    private String getText(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder stringBuilder = new StringBuilder();
            while (reader.ready()) stringBuilder.append(reader.readLine()).append('\n');
            return stringBuilder.toString();
        } catch (IOException e) {
            throw Exceptions.asUnchecked(e);
        }
    }

    private Consumer<Model> setCityInfo(City city) {
        return model -> {
            model.setCityId(city.getId());
            model.setVersion(city.getVersion());
        };
    }

    public void addAllCategories(City city, JSONObject content) {
        JSONArray category = content.getJSONArray("category");
        IntStream.range(0, category.size())
                .mapToObj(category::getJSONObject)
                .map(this::categoryMapper)
                .peek(setCityInfo(city))
                .forEach(categoryDao::insert);
    }

    public void addAllDistricts(City city, JSONObject content) {
        JSONArray district = content.getJSONArray("district");
        IntStream.range(0, district.size())
                .mapToObj(district::getJSONObject)
                .map(this::districtMapper)
                .peek(setCityInfo(city))
                .forEach(d -> {
                    districtDao.insertDistrict(d);
                    d.getCategories().forEach(category -> districtDao.insertDistrictCategory(
                            AppContext.uuid(), city.getId(), d.getId(), category.getId(), city.getVersion()));
                });
    }

    public void addFrequentSearch(City city, JSONObject content) {
        JSONArray freqsearch = content.getJSONArray("freqsearch");
        IntStream.range(0, freqsearch.size())
                .mapToObj(freqsearch::getString)
                .map(word -> {
                    FrequentSearch frequentSearch = new FrequentSearch();
                    frequentSearch.setId(AppContext.uuid());
                    frequentSearch.setCityId(city.getId());
                    frequentSearch.setWord(word);
                    frequentSearch.setVersion(city.getVersion());
                    return frequentSearch;
                }).forEach(frequentSearchDao::insert);
    }

    public void loadDataFromFile(City city, File dataFile, List<DataIndex> dataIndices) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(dataFile, "r")) {
            dataIndices.stream()
                    .map(dataIndex -> getDataString(randomAccessFile, dataIndex.offset, dataIndex.length))
                    .map(JSON::parseObject)
                    .map(this::subjectMapper)
                    .peek(setCityInfo(city))
                    .forEach(subject -> {
                        subjectDao.insertSubject(subject);
                        subject.getCategories().forEach(category -> subjectDao.insertSubjectCategory(
                                AppContext.uuid(), city.getId(), subject.getId(), category.getId(), city.getVersion())
                        );
                        subject.getTelephones().stream()
                                .peek(setCityInfo(city))
                                .peek(telephone -> telephone.setSubjectId(subject.getId()))
                                .forEach(subjectDao::insertSubjectTelephone);
                    });
        } catch (IOException e) {
            logger.warn("Exception throws on loading data file : " + dataFile.getAbsolutePath(), e);
            throw Exceptions.asUnchecked(e);
        }
    }

    private String getDataString(RandomAccessFile randomAccessFile, int offset, int length) {
        try {
            byte[] buffer = new byte[length];
            randomAccessFile.seek(offset);
            int len = randomAccessFile.read(buffer, 0, length);
            if (len != length) throw new EOFException("Reached EOF");
            byte[] bytes = decode(buffer, len, 8);
            return decompress(bytes);
        } catch (IOException e) {
            logger.info("Exception throws on read data file.", e);
            throw Exceptions.asUnchecked(e);
        }
    }

    private Category categoryMapper(JSONObject jsonObject) {
        Category category = new Category();
        category.setId(jsonObject.getString("id"));
        category.setName(jsonObject.getString("name"));
        category.setParentId(jsonObject.getString("pid"));
        category.setHot(jsonObject.getIntValue("hot"));
        return category;
    }

    private District districtMapper(JSONObject jsonObject) {
        District district = new District();
        district.setId(jsonObject.getString("id"));
        district.setName(jsonObject.getString("name"));
        JSONArray categoryIds = jsonObject.getJSONArray("cats");
        List<Category> categories = IntStream.range(0, categoryIds.size())
                .mapToObj(categoryIds::getString)
                .map(id -> {
                    Category category = new Category();
                    category.setId(id);
                    return category;
                }).collect(Collectors.toList());
        district.setCategories(categories);
        return district;
    }

    private Subject subjectMapper(JSONObject jsonObject) {
        Subject subject = new Subject();
        subject.setId(jsonObject.getString("id"));
        subject.setName(jsonObject.getString("name"));
        subject.setShortName(jsonObject.getString("shortname"));
        subject.setLogo(jsonObject.getString("logo"));
        JSONArray categoryIds = jsonObject.getJSONArray("cat_id");
        List<Category> categories = IntStream.range(0, categoryIds.size())
                .mapToObj(categoryIds::getString)
                .map(id -> {
                    Category category = new Category();
                    category.setId(id);
                    return category;
                }).collect(Collectors.toList());
        subject.setCategories(categories);
        JSONArray telephoneFields = jsonObject.getJSONArray("tel");
        List<Telephone> telephones = IntStream.range(0, telephoneFields.size())
                .mapToObj(telephoneFields::getJSONObject)
                .map(this::telephoneMapper)
                .collect(Collectors.toList());
        subject.setTelephones(telephones);
        return subject;
    }

    private Telephone telephoneMapper(JSONObject jsonObject) {
        Telephone telephone = new Telephone();
        telephone.setId(AppContext.uuid());
        telephone.setNumber(jsonObject.getString("tel_num"));
        telephone.setDescription(jsonObject.getString("tel_des"));
        telephone.setType(jsonObject.getIntValue("tel_type"));
        return telephone;
    }

    private List<DataIndex> getDataIndices(File indexFile) {
        try (InputStream inputStream = new FileInputStream(indexFile)){
            byte[] buffer = new byte[4096];
            int readLength = inputStream.read(buffer, 0, buffer.length);
            int headerLength = tail(buffer, readLength);
            byte[] headerBytes = decode(buffer, headerLength, 4);
            JSONObject header = JSON.parseObject(decompress(headerBytes));
            List<Integer> indexOffsets = getIndexOffsets(header);
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(indexFile, "r")) {
                return indexOffsets.stream()
                        .map(offset -> offset + headerLength)
                        .map(offset -> getDataIndices(randomAccessFile, offset))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            logger.warn("Exception throws on reading index file : " + indexFile, e);
            throw Exceptions.asUnchecked(e);
        }
    }

    private static List<Integer> getIndexOffsets(JSONObject jsonObject) {
        return jsonObject.keySet().stream()
                .map(jsonObject::getInteger)
                .collect(Collectors.toList());
    }

    private List<DataIndex> getDataIndices(RandomAccessFile randomAccessFile, int offset) {
        try {
            randomAccessFile.seek(offset);
            byte[] buffer = new byte[2048];
            int len = randomAccessFile.read(buffer, 0, buffer.length);
            byte[] data = decode(buffer, tail(buffer, len), 4);
            JSONArray jsonArray = JSON.parseArray(decompress(data));
            List<DataIndex> dataIndices = new ArrayList<>(jsonArray.size() / 2);
            for (int i = 0; i < jsonArray.size() - 1; i += 2) {
                dataIndices.add(new DataIndex(jsonArray.getIntValue(i), jsonArray.getIntValue(i + 1)));
            }
            return dataIndices;
        } catch (IOException e) {
            logger.warn("Exception throws on reading index file", e);
            throw Exceptions.asUnchecked(e);
        }
    }

    private static int tail(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
            if ((bytes[i] != 0) && (i > 1) && ((bytes[(i - 2)] | bytes[(i - 1)]) == 0)) {
                return i;
            }
        }
        return length;
    }

    private static byte[] decode(byte[] bytes, int arrayLength, int maskLength) {
        final byte[] mask = {31, -117, 8, 0, 0, 0, 0, 0, 0, 3};
        byte[] result = new byte[arrayLength + maskLength];
        System.arraycopy(mask, 0, result, 0, maskLength);
        System.arraycopy(bytes, 0, result, maskLength, arrayLength);
        return result;
    }

    private static String decompress(byte[] bytes) throws IOException {
        try (InputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private static class DataIndex {

        final int offset;
        final int length;

        public DataIndex(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        @Override
        public String toString() {
            return "[offset=" + offset + ", length=" + length + "]";
        }
    }
}
