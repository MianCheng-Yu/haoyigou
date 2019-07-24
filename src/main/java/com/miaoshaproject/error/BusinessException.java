package com.miaoshaproject.error;

/**
 * 包装器业务异常类的实现
 */
public class BusinessException extends Exception implements CommonError {

    private CommonError commonError;

    //直接接受EmBusinessError传参用于构造业务异常
    public BusinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    //接受自定义errorMsg构造业务异常
    public BusinessException(CommonError commonError, String erroeMsg) {
        super();
        this.commonError = commonError;
        this.setErrorMsg(erroeMsg);
    }

    @Override
    public int getErrorCode() {
        return this.commonError.getErrorCode();
    }

    @Override
    public String getErroeMsg() {
        return this.commonError.getErroeMsg();
    }

    @Override
    public CommonError setErrorMsg(String errorMsg) {
        this.commonError.setErrorMsg(errorMsg);
        return this;
    }
}
