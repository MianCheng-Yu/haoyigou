package com.miaoshaproject.error;

/**
 * 业务异常的枚举类
 */
public enum EmBusinessError implements CommonError {
    //通用错误类型 10001
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    UNKNOWN_ERROR(100002,"未知错误"),

    //20000开头为用户信息错误
    USER_NOT_EXIST(20001, "用户不存在"),
    LOGIN_VALIDATION_ERROR(20002, "用户名或密码错误"),
    USER_NOTLOGIN(20003, "用户未登录"),


    //30000开头为交易信息错误
    STOCK_NOT_ENOUGH(30001,"库存不足");

    EmBusinessError(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    private int errorCode;
    private String errorMsg;

    @Override
    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErroeMsg() {
        return this.errorMsg;
    }

    @Override
    public CommonError setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return null;
    }
}
