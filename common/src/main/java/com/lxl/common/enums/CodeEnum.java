package com.lxl.common.enums;

/**
 * @ClassName CodeEnum
 * @Author Zhidan.Rao
 * @Date 2018年07月06日 15:26
 * @Version 1.0.0
 **/
public enum CodeEnum {
    SUCCESS("1", "操作成功"),
    ERROR("0", "操作失败"),
    SERVICE_ERROR("500", "服务内部错误"),
    FLOW_ERROR("1000", "服务器繁忙，请稍后重试"),
    NOT_LOGIN("1001", "未登录系统"),
    PARAM_ERROR("1002", "参数错误"),
    NO_PERMISSION("1003", "权限不足"),
    TIME_OUT("1010", "服务器超时，请重试");

    public String code;
    public String message;

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

    CodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
