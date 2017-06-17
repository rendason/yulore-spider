package org.tafia.spider.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tafia.spider.model.District;

/**
 * Created by Dason on 2017/6/17.
 */
@Mapper
public interface DistrictDao {

    @Insert("INSERT IGNORE INTO district VALUES(#{id}, #{cityId}, #{name}, #{version})")
    void insertDistrict(District district);

    @Insert("INSERT INTO district_category VALUES(#{id}, #{cityId}, #{districtId}, #{categoryId}, #{version})")
    void insertDistrictCategory(@Param("id") String id,
                                @Param("cityId") String cityId,
                                @Param("districtId") String districtId,
                                @Param("categoryId") String categoryId,
                                @Param("version") int version);
}
