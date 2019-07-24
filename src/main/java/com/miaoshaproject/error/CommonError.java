package com.miaoshaproject.error;

public interface CommonError {
    public int getErrorCode();
    public String getErroeMsg();
    public CommonError setErrorMsg(String errorMsg);

}
