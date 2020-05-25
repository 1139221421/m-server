package com.lxl.common.enums;

@SuppressWarnings("ALL")
public enum MqTagsEnum {
    TEST("TEST", "测试");

    private String tagName;

    private String tagDesc;

    MqTagsEnum(String tagName, String desc) {
        this.tagName = tagName;
        this.tagDesc = desc;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagDesc() {
        return tagDesc;
    }

    public void setTagDesc(String tagDesc) {
        this.tagDesc = tagDesc;
    }

}
