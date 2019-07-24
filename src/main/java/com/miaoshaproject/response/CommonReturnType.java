package com.miaoshaproject.response;

public class CommonReturnType {


    private String status;//返回状态
    private Object data;//返回数据

    //当status=success，data返回json数据
    //当status=fall，data返回通用错误格式码

    /**
     * 只带data 数据的create方法
     *
     * @param result
     *
     * @return
     */
    public static CommonReturnType create(Object result) {
        return CommonReturnType.create(result, "success");
    }

    /**
     * 带status和data数据的create方法
     *
     * @param result
     * @param status
     * @return
     */
    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setData(result);
        type.setStatus(status);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
