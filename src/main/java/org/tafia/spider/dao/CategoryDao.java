package org.tafia.spider.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.tafia.spider.model.Category;

/**
 * Created by Dason on 2017/6/17.
 */
@Mapper
public interface CategoryDao {

    @Insert("INSERT IGNORE INTO category VALUES(#{id}, #{cityId}, #{name}, #{parentId}, #{hot}, 0, #{version})")
    void insert(Category category);
}
