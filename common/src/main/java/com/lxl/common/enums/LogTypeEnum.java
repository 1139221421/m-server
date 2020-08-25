package com.lxl.common.enums;

public enum LogTypeEnum {
    SYSTEM_LOG("系统日志", "用户【%s】IP【%s】操作系统【%s】"),
    LOGIN_LOG("登录登录", "用户【%s】IP【%s】登录系统【%s】"),
    CREATE("插入记录", "用户【%s】插入了数据,IP【%s】"),
    UPDATE("更新记录", "用户【%s】更新了数据,IP【%s】"),
    DELETE("删除记录", "用户【%s】删除了数据,IP【%s】");

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
