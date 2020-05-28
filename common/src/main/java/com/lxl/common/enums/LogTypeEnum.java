package com.lxl.common.enums;

public enum LogTypeEnum {
    SYSTEM_LOG("系统日志", "用户【%s】IP【%s】操作系统【%s】"),
    LOGIN_LOG("登录登录", "用户【%s】IP【%s】登录系统【%s】");

    LogTypeEnum(String logName, String pattern) {
        this.logName = logName;
        this.pattern = pattern;
    }

    private String logName;

    /**
     * 内容格式
     * 如：【类名】:%s,【方法】:%s,【参数】:%s,【IP】:%s
     */
    private String pattern;

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
