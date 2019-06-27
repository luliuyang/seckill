package com.seckill.service;

import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"
})
public class SeckillServiceTest {
    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> seckills= seckillService.getSeckillList();
        logger.info("seckills={}",seckills);
        //INFO  c.seckill.service.SeckillServiceTest - seckills=[Seckill{seckillId=1000, name='8000元秒杀iphonex', createTime=Thu May 16 22:52:51 CST 2019, startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Sun Mar 17 08:00:00 CST 2019}, Seckill{seckillId=1001, name='3500元秒杀ipad', createTime=Thu May 16 22:52:51 CST 2019, startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Mon Mar 18 08:00:00 CST 2019}, Seckill{seckillId=1002, name='18000元秒杀mac book pro', createTime=Thu May 16 22:52:51 CST 2019, startTime=Thu Mar 28 08:00:00 CST 2019, endTime=Fri Mar 29 08:00:00 CST 2019}, Seckill{seckillId=1003, name='15000元秒杀iMac', createTime=Thu May 16 22:52:51 CST 2019, startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Fri Mar 29 08:00:00 CST 2019}]
    }

    @Test
    public void getById() {
        long id=1000L;
        Seckill seckill=seckillService.getById(id);
        logger.info("seckill={}",seckill);
        //INFO  c.seckill.service.SeckillServiceTest - seckill=Seckill{seckillId=1000, name='8000元秒杀iphonex', createTime=Thu May 16 22:52:51 CST 2019, startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Sun Mar 17 08:00:00 CST 2019}
    }

//    @Test
//    public void exportSeckillUrl() {
//        long seckillId = 1000L;
//        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
//        logger.info("exposer={}", exposer);
//        //c.seckill.service.SeckillServiceTest - exposer=Exposer{exposed=false, md5='null', seckillId=1001, now=1559203867993, start=1552608000000, end=1552867200000}
//    }

//    @Test
//    public void executeSeckill() {
//        long seckillId = 1001L;
//        long ph = 13567688594L;
//        String md5="";
//        SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId,ph,md5);
//        logger.info("seckillExecution={}", seckillExecution);
//    }

    // 集成测试代码完整逻辑，注意可重复执行
    @Test
    public void testSeckillLogic() throws Exception {
        long seckillId = 1001L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long userPhone = 13476191576L;
            String md5 = exposer.getMd5();
            try {
                SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
                logger.info("result={}", execution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e1) {
                logger.error(e1.getMessage());
            }
        } else {
            // 秒杀未开启
            logger.warn("exposer={}", exposer);
        }
    }
    @Test
    public void executeSeckillProcedure() {
        long seckillId = 1000L;
        long phone = 13680115101L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            logger.info(execution.getStateInfo());
        }
    }
}