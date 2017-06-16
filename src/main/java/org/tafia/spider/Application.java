package org.tafia.spider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tafia.spider.dao.CityDao;
import org.tafia.spider.model.City;

import javax.annotation.PostConstruct;

/**
 * Created by Dason on 2017/6/16.
 */
@SpringBootApplication
public class Application {

    @Autowired
    private CityDao cityDao;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        City city = new City();
        city.setId(AppContext.uuid());
        city.setName("city");
        city.setParentId("0");
        city.setAreaCode("100001");
        city.setFullPinyin("city");
        city.setShortPinyin("CT");
        city.setHot(0);
        city.setVersion(100);
        cityDao.insert(city);
    }
}
