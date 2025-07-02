package org.bigmouth.gpt.exceptions;

import com.bxm.warcar.integration.eventbus.EventListener;
import com.bxm.warcar.integration.eventbus.core.AllowConcurrentEvents;
import com.bxm.warcar.integration.eventbus.core.Subscribe;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ai.entity.ApiKey;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.event.ChatRetryEvent;
import org.bigmouth.gpt.exceptions.msg.ErrorMsgRegexHandler;
import org.bigmouth.gpt.exceptions.msg.ErrorMsgRegexHandlerFactory;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author allen
 * @date 2023/5/6
 * @since 1.0.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler implements EventListener<ChatRetryEvent> {

    private final ErrorMsgRegexHandlerFactory errorMsgRegexHandlerFactory;

    private final Map<String, Pattern> patternMap = Maps.newHashMap();

    public GlobalExceptionHandler(ErrorMsgRegexHandlerFactory errorMsgRegexHandlerFactory) {
        this.errorMsgRegexHandlerFactory = errorMsgRegexHandlerFactory;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void consume(ChatRetryEvent event) {
        AiAccountException exception = event.getException();
        ErrorMsgRegexHandler handler = findErrorMsgHandler(exception);
        executeHandlerAndReturnErrorMsg(exception, handler);
    }

    // ----------------------------- 其他未知异常 ----------------------------- //

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception exception) {
        log.error("Unknown Exception: ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }

    // ----------------------------- TalkX 系统异常 ----------------------------- //

    @ExceptionHandler(TalkxException.class)
    public ResponseEntity<Object> talkxException(TalkxException exception) {
        User user = ContextFactory.getLoginUser();
        String phoneNum = Optional.ofNullable(user).map(User::getPhoneNum).orElse("-");
        log.info("400 {} - {}", phoneNum, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CoinNotEnoughException.class)
    public ResponseEntity<Object> coinNotEnoughException(CoinNotEnoughException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("蒜粒不足。你可以进入个人中心-我的蒜粒进行充值。");
    }

    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<String> illegalStateException(Throwable throwable) {
        log.warn("{}", throwable.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(throwable.getMessage());
    }

    // ----------------------------- AI 平台异常 ----------------------------- //

    @ExceptionHandler(AiException.class)
    public ResponseEntity<Object> aiException(AiException exception) {
        log.error("AIException: ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    @ExceptionHandler(AiStatusException.class)
    public ResponseEntity<Object> aiStatusException(AiStatusException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    @ExceptionHandler(AiNetworkException.class)
    public ResponseEntity<Object> aiNetworkException(AiNetworkException exception) {
        log.warn("io: ", exception);
        String respMsg = "抱歉！AI平台网络拥堵，请稍后重试。";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respMsg);
    }

    @ExceptionHandler(AiAccountException.class)
    public ResponseEntity<Object> aiAccountException(AiAccountException exception) {
        ApiKey apiKey = exception.getApiKey();
        if (Objects.isNull(apiKey)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }

        String accessKey = apiKey.getApiKey();

        String message = exception.getMessage();
        log.warn("{} - sc: {} message: {}", accessKey, exception.getSc(), message);

        ErrorMsgRegexHandler handler = findErrorMsgHandler(exception);
        String res = executeHandlerAndReturnErrorMsg(exception, handler);

        String respMsg = Optional.ofNullable(res).orElse("抱歉！TalkX连接AI平台的账户似乎出现了些问题，请1分钟后重试。");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respMsg);
    }

    /**
     * 执行handler的afterHandler方法，并返回它的消息内容
     * @param exception 异常
     * @param handler 执行器
     * @return handler的自定义消息
     */
    public String executeHandlerAndReturnErrorMsg(AiAccountException exception, ErrorMsgRegexHandler handler) {
        String res = null;
        if (Objects.nonNull(handler)) {
            String handlerMessage = handler.getMessage(exception);
            if (StringUtils.isNotBlank(handlerMessage)) {
                res = handlerMessage;
            }
            handler.afterHandle(exception);
        }
        return res;
    }

    /**
     * 根据异常的消息内容或者状态码来找到对应的Handler
     * @param exception 异常
     * @return ErrorMsgRegexHandler
     */
    public ErrorMsgRegexHandler findErrorMsgHandler(AiAccountException exception) {
        Collection<ErrorMsgRegexHandler> handlers = errorMsgRegexHandlerFactory.getAll();
        ErrorMsgRegexHandler defnied = null;
        // 先对正则表达式优先处理
        for (ErrorMsgRegexHandler handler : handlers) {
            String regex = handler.getRegex();
            if (StringUtils.isNotBlank(regex)) {
                Pattern pattern = patternMap.computeIfAbsent(regex, s -> Pattern.compile(regex));
                Matcher matcher = pattern.matcher(exception.getMessage());
                if (matcher.find()) {
                    defnied = handler;
                    break;
                }
            }
        }
        // 如果没有匹配的正则表达式再按状态码处理
        if (Objects.isNull(defnied)) {
            for (ErrorMsgRegexHandler handler : handlers) {
                int statusCode = handler.getStatusCode();
                if (statusCode != -1 && exception.getSc() == statusCode) {
                    defnied = handler;
                    break;
                }
            }
        }
        return defnied;
    }

    // ----------------------------- 参数校验异常 ----------------------------- //

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, DateTimeParseException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<String> illegalArgument(Throwable throwable) {
        log.warn("{}", throwable.getMessage(), throwable);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(throwable.getMessage());
    }

    @ExceptionHandler({BindException.class})
    public ResponseEntity<String> bindException(BindException throwable) {
        log.warn("{}", throwable.getMessage());
        StringBuilder msg = new StringBuilder();
        List<ObjectError> errors = throwable.getAllErrors();
        for (ObjectError error : errors) {
            msg.append(error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg.toString());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<String> methodArgumentNotValidException(MethodArgumentNotValidException throwable) {
        log.warn("{}", throwable.getMessage());
        StringBuilder msg = new StringBuilder();
        BindingResult bindingResult = throwable.getBindingResult();
        List<ObjectError> errors = bindingResult.getAllErrors();
        for (ObjectError error : errors) {
            msg.append(error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg.toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> constraintViolationException(ConstraintViolationException throwable) {
        log.warn("{}", throwable.getMessage());
        StringBuilder msg = new StringBuilder();
        Set<ConstraintViolation<?>> constraintViolations = throwable.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            msg.append(constraintViolation.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg.toString());
    }
}
