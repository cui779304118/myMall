package cup.cw.mall.controller;

import cup.cw.mall.common.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * created by cuiwei on 2018/11/1
 */
@ControllerAdvice
public class ControllerExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);

    /**
     * 全局异常捕捉处理
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ServerResponse handleException(Exception e){
        logger.error("出现未知错误",e);
        return ServerResponse.createByError("操作错误！错误原因：" + e.getMessage());
    }


}
