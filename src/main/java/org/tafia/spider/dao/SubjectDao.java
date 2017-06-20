package org.tafia.spider.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import org.tafia.spider.model.Subject;
import org.tafia.spider.model.Telephone;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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

    @InsertProvider(type = BatchInsertProvider.class, method = "telephone")
    void insertSubjectTelephones(List<Telephone> telephone);

    class BatchInsertProvider {

        public String telephone(Map<String, Object> map) {
            List<Telephone> list = (List<Telephone>) map.get("list");
            StringBuilder stringBuilder = new StringBuilder("INSERT INTO subject_telephone VALUES");
            MessageFormat messageFormat = new MessageFormat("(#'{'list[{0}].id}, #'{'list[{0}].cityId}, " +
                    "#'{'list[{0}].subjectId}, #'{'list[{0}].number}, #'{'list[{0}].description}, #'{'list[{0}].type}, #'{'list[{0}].version}),");
            IntStream.range(0, list.size()).forEach(i -> stringBuilder.append(messageFormat.format(new Object[]{i})));
            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }

    }
}
