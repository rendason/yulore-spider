package org.tafia.spider.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.tafia.spider.model.City;

/**
 * City Dao
 */
@Mapper
public interface CityDao {

    @Insert("INSERT INTO city (id, name, pid, area_code, hot, pinyin_short, pinyin_full, version) " +
            "VALUES(#{id}, #{name}, #{parentId}, #{areaCode}, #{hot}, #{shortPinyin}, #{fullPinyin}, #{version})")
    void insert(City city);
}
