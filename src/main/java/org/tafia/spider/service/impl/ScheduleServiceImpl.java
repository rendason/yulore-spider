package org.tafia.spider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tafia.spider.dao.CityDao;
import org.tafia.spider.model.City;
import org.tafia.spider.service.DataUpdateService;
import org.tafia.spider.service.HttpDownloadService;
import org.tafia.spider.service.ScheduleService;
import org.tafia.spider.util.Functions;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 调度服务实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${yulore.metadata.city}")
    private String metadataUrl;

    @Autowired
    private HttpDownloadService httpDownloadService;

    @Autowired
    private DataUpdateService dataUpdateService;

    @Autowired
    private CityDao cityDao;

    private Map<String, Boolean> updateStatusMap = new ConcurrentHashMap<>();

    private AtomicBoolean isUpdating = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        logger.info("Initialized metadata pull url : " + metadataUrl);
    }

    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public void schedule() {
        update();
    }

    @Async
    @Override
    public void refresh() {
        update();
    }

    @Override
    public Map<String, String> status() {
        return updateStatusMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() ? "正在更新" : "更新结束"));
    }

    private void update() {
        if (!isUpdating.compareAndSet(false, true)) {
            logger.info("The task of update is running, skip this time.");
            return;
        }
        logger.info("Start to update offline data from " + metadataUrl);
        try {
            String metadata = httpDownloadService.asString(metadataUrl);
            List<City> cities = getCitiesFromMetadata(metadata);
            cities.stream().filter(this::versionPredicate).forEach(this::update);
        } catch (Exception e) {
            logger.warn("Exception throws on updating offline data", e);
        } finally {
            isUpdating.set(false);
        }

    }

    private boolean versionPredicate(City city) {
        Integer version = cityDao.maxVersion(city.getId());
        return version == null || city.getVersion() > version;
    }

    private void update(City city) {
        if (updateStatusMap.put(city.getName(), Boolean.TRUE) == Boolean.TRUE) {
            logger.info("The offline data of {} is updating, skip this time.", city.getName());
            return;
        }
        try {
            dataUpdateService.update(city);
        } finally {
            updateStatusMap.put(city.getName(), Boolean.FALSE);
        }
    }

    private List<City> getCitiesFromMetadata(String metadata) {
        JSONObject jsonObject = JSON.parseObject(metadata);
        JSONArray jsonArray = jsonObject.getJSONArray("cities");
        if (jsonArray.isEmpty()) return Collections.emptyList();
        return IntStream.range(0, jsonArray.size())
                .mapToObj(jsonArray::getJSONObject)
                .map(this::cityMapper)
                .collect(Collectors.toList());
    }

    private City cityMapper(JSONObject jsonObject) {
        City city = new City();
        city.setId(jsonObject.getString("id"));
        city.setName(jsonObject.getString("name"));
        city.setParentId(jsonObject.getString("pid"));
        city.setAreaCode(jsonObject.getString("areacode"));
        city.setFullPinyin(jsonObject.getString("pyf"));
        city.setShortPinyin(jsonObject.getString("pys"));
        city.setHot(jsonObject.getIntValue("hot"));
        city.setPackagee(jsonObject.getString("pkg"));
        city.setVersion(jsonObject.getIntValue("ver"));
        city.setTimestamp(System.currentTimeMillis());
        return city;
    }
}
