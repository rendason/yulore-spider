package org.tafia.spider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tafia.spider.model.City;
import org.tafia.spider.service.DataUpdateService;
import org.tafia.spider.service.HttpDownloadService;
import org.tafia.spider.service.ScheduleService;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
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

    @PostConstruct
    public void init() {
        logger.info("Initialized metadata pull url : " + metadataUrl);
    }

    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public void schedule() {
        update();
    }

    @Override
    public void refresh() {
        update();
    }

    private void update() {
        logger.info("Start to update offline data from " + metadataUrl);
        String metadata = httpDownloadService.asString(metadataUrl);
        if (metadata == null) {
            logger.warn("Can not pull metadata from " + metadataUrl);
            return;
        }
        List<City> cities = getCitiesFromMetadata(metadata);
        cities.forEach(dataUpdateService::update);
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
        return city;
    }
}
