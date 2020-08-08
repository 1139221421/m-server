package com.lxl.common.enums;

/**
 * @ClassName CodeEnum
 * @Author Zhidan.Rao
 * @Date 2018年07月06日 15:26
 * @Version 1.0.0
 **/
public enum CodeEnum {
    SUCCESS("1", "操作成功",true),
    ERROR("0", "操作失败",false),
    SERVICE_ERROR("500", "服务内部错误",false),
    FLOW_ERROR("1000", "服务器繁忙，请稍后重试",false),
    NOT_LOGIN("1001", "未登录系统",false),
    PARAM_ERROR("1002", "参数错误",false),
    NO_PERMISSION("1003", "权限不足",false),
    TIME_OUT("1010", "服务器超时，请重试",false);

    public String code;
    public String message;
    public boolean success;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    CodeEnum(String code, String message, boolean success) {
        this.code = code;
        this.message = message;
        this.success = success;
    }
}
