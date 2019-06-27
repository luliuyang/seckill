package com.seckill.entity;

import java.util.Date;

/**
 * @author lulu
 * @date 2019-05-16 15:02
 */
public class SuccessKilled {
    private long seckillId;
    private Date createTime;
    private short state;
    private long userPhone;
    /**
     *JAVA的实体对应着数据库中的表，属性对应相应的列
     * 多对一，因为一件商品seckill在库存中有很多数量，对用的购买明细SuccessKilled也有很多
     */
    private Seckill seckill;

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(long userPhone) {
        this.userPhone = userPhone;
    }

    public Seckill getSeckill() {
        return seckill;
    }

    public void setSeckill(Seckill seckill) {
        this.seckill = seckill;
    }

    @Override
    public String toString() {
        return "SuccessKilled{" +
                "seckillId=" + seckillId +
                ", createTime=" + createTime +
                ", state=" + state +
                ", userPhone=" + userPhone +
                ", seckill=" + seckill +
                '}';
    }
}
