package com.seckill.dao;

import com.seckill.entity.Seckill;
import org.apache.commons.collections.MapUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.swing.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
/**
 * 配置spring和junit整合，junit启动时加载IOC容器
 * spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})

public class SeckillDaoTest {
    //注入依赖
    @Resource
    private SeckillDao seckillDao;
    @Test
    public void queryById() {
        long id=1000;
        Seckill s=seckillDao.queryById(id);
        System.out.println(s.getName());
        System.out.println(s.toString());
    }

    @Test
    public void reduceNumber() {
        long id=1000;
        Date d=new Date();
        int a=seckillDao.reduceNumber(id,d);
        System.out.println(a);
    }



    @Test
    public void queryAll() {
        List<Seckill> ans= seckillDao.queryAll(0,100);
        for (Seckill seckill : ans) {
            System.out.println(seckill);
        }
    }

    @Test
    public void killByProcedure() {
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", 1000);
        map.put("phone", 13567895461L);
        map.put("killTime", new Date());
        map.put("result", null);
        seckillDao.killByProcedure(map);
        int result = MapUtils.getInteger(map, "result", -2);
        System.out.println(result);
    }
}