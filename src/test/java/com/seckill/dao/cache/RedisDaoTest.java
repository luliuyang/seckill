package com.seckill.dao.cache;


import com.seckill.dao.SeckillDao;
import com.seckill.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})

public class RedisDaoTest {
    private long id=1002L;
    @Autowired
    private  RedisDao redisDao;
    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void testtSeckill() throws Exception{
        //因为现在缓存是空的，先通过seckillDao拿到一个秒杀对象，然后缓存进去
        Seckill seckill=redisDao.getSeckill(id);
        if(seckill==null){
            seckill=seckillDao.queryById(id);
            if(seckill!=null){
                String rst=redisDao.putSeckill(seckill);
                System.out.println(rst);
                seckill=redisDao.getSeckill(id);
                System.out.println(seckill);
            }
        }

    }


}