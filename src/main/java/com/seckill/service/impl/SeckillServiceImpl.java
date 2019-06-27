package com.seckill.service.impl;

import com.seckill.dao.SeckillDao;
import com.seckill.dao.SuccessKilledDao;
import com.seckill.dao.cache.RedisDao;
import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.entity.SuccessKilled;
import com.seckill.enums.SeckillStatEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.exception.SeckillException;
import com.seckill.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lulu
 * @date 2019-05-30 14:42
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    /**
     * 混淆的”盐“
     */
    private final String slat = "fjkdnc/jldfa9";
    /**
     * 基于sl4j的日志文件
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {


        return seckillDao.queryById(seckillId);
    }

    //    @Override
//    public Exposer exportSeckillUrl(long seckillId) {
//        Seckill seckill = seckillDao.queryById(seckillId);
//        if (seckill == null) {
//            //没有这个秒杀商品
//            return new Exposer(false, seckillId);
//        }
//        Date startTime = seckill.getStartTime();
//        Date endTime = seckill.getEndTime();
//        Date now = new Date();
//        if (now.getTime() < startTime.getTime() || now.getTime() > endTime.getTime()) {
//            //当前的时间小于秒杀开启时间或者大于结束时间，说明不是正在进行时，不是有效参与时间
//            return new Exposer(false, seckillId, now.getTime(), startTime.getTime(), endTime.getTime());
//        }
//        //正在进行时，所以需要返回一个秒杀暴露接口
//        //转化特性字符串的过程，不可逆，
//        String md5 = getmd5(seckillId);
//        return new Exposer(true, md5, seckillId);
//    }
    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存点 超时的基础上维护一致性  减低了数据库访问量
        // 1.访问redi
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            // 2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                // 说明查不到这个秒杀产品的记录
                return new Exposer(false, seckillId);
            } else {
                // 3.放入redis
                redisDao.putSeckill(seckill);
            }
        }
        /*
        Seckill seckill = seckillDao.queryById(seckillId);
        if (seckill == null) {
            return new Exposer(false, seckillId);
        }*/
        // 若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        // 系统当前时间
        Date nowTime = new Date();
        if (startTime.getTime() > nowTime.getTime() || endTime.getTime() < nowTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        // 秒杀开启，返回秒杀商品的id、用给接口加密的md5
        //转化特定字符的过程，不可逆
        String md5 = getmd5(seckillId);
        return new Exposer(true, md5, seckillId);
    }


    private String getmd5(long seckillId) {
        String base = seckillId + "/" + slat;

        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getmd5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录秒杀行为
        Date now = new Date();
        try {
//            int updatecnt = seckillDao.reduceNumber(seckillId, now);
//            if (updatecnt <= 0) {
//                //没有更新到记录，可能秒杀结束了啥的
//                throw new SeckillException("seckill is closed");
//            } else {
//                //减库存成功了，记录秒杀行为
//                int insercnt = successKilledDao.insertSuccessKilled(seckillId, userPhone);
//                //唯一：seckillId,uderphone
//                if (insercnt <= 0) {
//                    throw new RepeatKillException("seckill repeated");
//                } else {
//                    //秒杀成功了
//                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
//                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
//                }
//            }
            //优化先insert购买明细，然后再减库存
            int insercnt = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insercnt <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                //减库存，热点商品竞争
                int updatecnt = seckillDao.reduceNumber(seckillId, now);
                if (updatecnt <= 0) {
                    //没有更新到记录，可能秒杀结束了啥的
                    throw new SeckillException("seckill is closed");
                } else {
                    //秒杀成功了
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 将编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }

    }

    /**
     * 新的执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
     *
     * @param seckillId 秒杀的商品ID
     * @param userPhone 手机号码
     * @param md5       md5加密值
     * @return 根据不同的结果返回不同的实体信息
     */
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getmd5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行储存过程,result被复制
        try {
            seckillDao.killByProcedure(map);
            // 获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);

        }
    }
}
