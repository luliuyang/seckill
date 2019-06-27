package com.seckill.enums;

/**
 * 使用枚举表述常量数据字段
 */
public enum SeckillStatEnum {
    /**
     * 枚举类的所有实例必须在枚举类的第一行显示列出，否则这个枚举类永远都不能产生实例，系统会自动添加public static final进行修饰，无需显式添加
     */

    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATE_REWRITE(-3, "数据篡改");

    private int state;
    private String info;

    /**
     * 枚举类的狗在其只能使用且默认使用 private修饰
     * @param state
     * @param info
     */
    SeckillStatEnum(int state, String info) {
        this.state = state;
        this.info = info;
    }

    public int getState() {
        return state;
    }

    public String getInfo() {
        return info;
    }

    public static SeckillStatEnum stateOf(int index) {
        //枚举类默认提供了一个values()方法，可以方便遍历所有枚举值
        for (SeckillStatEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }
}
