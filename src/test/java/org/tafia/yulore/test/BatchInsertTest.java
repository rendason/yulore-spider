package org.tafia.yulore.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tafia.spider.Application;
import org.tafia.spider.dao.SubjectDao;
import org.tafia.spider.model.Telephone;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Dason on 2017/6/20.
 */
@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class BatchInsertTest {

    @Autowired
    private SubjectDao subjectDao;

    @Test
    public void testTelephone() {
        List<Telephone> telephones = Arrays.asList(new Telephone(), new Telephone());
        subjectDao.insertSubjectTelephones(telephones);
    }
}
