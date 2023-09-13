
package cn.cfl.memory.exception;

import cn.cfl.memory.http.RestStructure;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器
 *
 * @author chen.fangliang
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(Exception.class)
    public RestStructure<Void> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return RestStructure.error(-1, e.getMessage());
    }

}
