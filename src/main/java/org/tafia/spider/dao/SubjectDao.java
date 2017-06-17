package org.tafia.spider.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tafia.spider.model.Subject;
import org.tafia.spider.model.Telephone;

/**
 * Created by Dason on 2017/6/17.
 */
@Mapper
public interface SubjectDao {

    @Insert("INSERT IGNORE INTO subject VALUES(#{id}, #{cityId}, #{name}, #{shortName}, #{logo}, #{version})")
    void insertSubject(Subject subject);

    @Insert("INSERT INTO subject_category VALUES(#{id}, #{cityId}, #{subjectId}, #{categoryId}, #{version})")
    void insertSubjectCategory(@Param("id") String id,
                               @Param("cityId") String cityId,
                               @Param("subjectId") String subjectId,
                               @Param("categoryId") String categoryId,
                               @Param("version") int version);

    @Insert("INSERT INTO subject_telephone VALUES(#{id}, #{cityId}, #{subjectId}, #{number}, #{description}, #{type}, #{version})")
    void insertSubjectTelephone(Telephone telephone);
}
