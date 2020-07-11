package com.github.pure.cm.common.core.enums;

/**
 * 创建:熊黄平
 * 时间:2020/7/3 10:57
 * 功能描述:应用
 */
public enum AppCode {

    /**
     * 系统内部异常:10000
     */
    SYSTEM_INNER(10000, "鉴权服务"),
    /**
     * 系统管理 ： 20000
     */
    AUTH_SERVER(20000, "鉴权服务");
    /**
     * 值
     */
    private final int value;
    /**
     * 名称
     */
    private final String label;

    AppCode(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
