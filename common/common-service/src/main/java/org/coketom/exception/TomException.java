package org.coketom.exception;


import lombok.Data;
import org.coketom.vo.common.ResultCodeEnum;

@Data
public class TomException {
    private Integer code;
    private String message;
    private ResultCodeEnum resultCodeEnum;

    public TomException(ResultCodeEnum resultCodeEnum){
        this.resultCodeEnum = resultCodeEnum;
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}
