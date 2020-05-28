package com.lxl.common.entity.log;

import java.util.Date;

public class Log {

    private Long id;
    /**
     * 日志内容
     */
    private String content;
    /**
     * 操作类型
     */
    private String logType;

    /**
     * 操作状态
     */
    private Boolean status;

    /**
     * 操作人姓名
     */
    private String creatorName;

    /**
     * 操作人
     */
    private String creatorId;

    /**
     * 操作时间
     */
    private Date createTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
