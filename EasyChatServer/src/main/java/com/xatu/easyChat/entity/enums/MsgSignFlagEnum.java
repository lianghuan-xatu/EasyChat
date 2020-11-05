package com.xatu.easyChat.entity.enums;

public enum MsgSignFlagEnum
{
    unsign(0,"未签收"),
    sign(1,"已签收");
    private Integer status;
    private String info;

    MsgSignFlagEnum(Integer status, String info) {
        this.status = status;
        this.info = info;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
