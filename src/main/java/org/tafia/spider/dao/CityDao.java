package org.tafia.spider.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.tafia.spider.model.City;

/**
 * City Dao
 */
@Mapper
public interface CityDao {

    @Insert("INSERT IGNORE INTO city (id, name, pid, area_code, hot, package, pinyin_short, pinyin_full, version, timestamp) " +
            "VALUES(#{id}, #{name}, #{parentId}, #{areaCode}, #{hot}, #{packagee}, #{shortPinyin}, #{fullPinyin}, #{version}, #{timestamp})")
    void insert(City city);

    @Select("SELECT MAX(version) FROM city WHERE id = #{cityId}")
    Integer maxVersion(String cityId);
}
