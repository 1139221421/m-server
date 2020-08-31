package com.lxl.common.enums;

public enum TransactionEnum {
    PREPARE(1, "已准备"),
    COMMIT(2, "已提交"),
    ROLLBACK(3, "已回滚");


    private int state;
    private String desc;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    TransactionEnum(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }
}
