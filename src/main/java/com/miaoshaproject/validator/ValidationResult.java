package com.miaoshaproject.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    private Boolean hasError =false;
    private Map<String, String> errorMsgMap = new HashMap<>();

    public boolean isHasErrors() {
        return hasError;
    }
    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public Map<String, String> getErrorMsgMap() {
        return errorMsgMap;
    }

    public void setErrorMsgMap(Map<String, String> errorMsgMap) {
        this.errorMsgMap = errorMsgMap;
    }

    //实现通用的通过字符串信息获取所有错误信息的方法
    public String getErrMag() {
        return StringUtils.join(errorMsgMap.values().toArray(),",");
    }

}
