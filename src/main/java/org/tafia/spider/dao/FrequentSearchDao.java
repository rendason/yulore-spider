package org.tafia.spider.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.tafia.spider.model.FrequentSearch;

/**
 * Created by Dason on 2017/6/17.
 */
@Mapper
public interface FrequentSearchDao {

    @Insert("INSERT IGNORE INTO frequent_search VALUES(#{id}, #{cityId}, #{word}, #{version})")
    void insert(FrequentSearch frequentSearch);
}
