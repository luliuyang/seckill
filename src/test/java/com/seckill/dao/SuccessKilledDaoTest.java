package com.seckill.dao;

import com.seckill.entity.SuccessKilled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})

public class SuccessKilledDaoTest {
    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() {
        long id=1001L;
        long up=13567628301L;

        int ans=successKilledDao.insertSuccessKilled(id,up);
        System.out.println(ans);
    }

    @Test
    public void queryByIdWithSeckill() {
        long id=1001L;
        long up=13567628301L;
        SuccessKilled ans=successKilledDao.queryByIdWithSeckill(id,up);
        System.out.println(ans);
        System.out.println(ans.getSeckill());
    }
}