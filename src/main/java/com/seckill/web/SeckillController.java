package com.seckill.web;

import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.dto.SeckillResult;
import com.seckill.entity.Seckill;
import com.seckill.enums.SeckillStatEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author lulu
 * @date 2019-06-03 11:34
 */
//为了放入spring容器当中
@Controller
@RequestMapping(value = "/seckill")
public class SeckillController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        //list.jsp+model=ModelandView
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        //"/WEB-INF/jsp/list.sjp"
        return "list";
    }
    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            //通过字符串控制，请求重定向
            return "redirect:/seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            //通过字符串控制，请求转发
            return "forward:/seckill/list";
        }
        //传入了“seckill"和对象，通过key-value这样的方式进行映射
        model.addAttribute("seckill", seckill);
        return "detail";
    }


    /**
     * ajax接口,返回类型是json,这里老师用的方法时post,但是服务器显示405错误，应该改成get,后来又405了，Request method 'POST' not supported，又改成post
     *  //@ResponseBody的作用其实是将java对象转为json格式的数据。
     */
    @RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.POST,
            produces = {"application/json;charset-utf-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset-utf-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") long seckillId,
                                                   @CookieValue(value = "userPhone", required = false) Long userPhone,
                                                   @PathVariable("md5") String md5) {
        if (userPhone == null) {
            return new SeckillResult<SeckillExecution>(false, "未注册");
        }
        SeckillResult<SeckillExecution> result;
        try {
            //存储过程调用
            SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(seckillId, userPhone, md5);
            result = new SeckillResult<SeckillExecution>(true, seckillExecution);
        } catch (RepeatKillException e1) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillCloseException e2) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<SeckillExecution>(true, e.getMessage());
        }
        return result;
    }

    //获取系统时间
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }
}
