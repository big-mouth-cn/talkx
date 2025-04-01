package com.bxm.warcar.integration.eventbus.micrometer;

import com.bxm.warcar.integration.eventbus.EventListener;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 事件监听器切面
 * @author allen
 * @date 2019/6/20
 * @since 1.0.0
 */
@Aspect
public class EventListenerMeterAspect {

    private final EventListenerMeter eventListenerMeter;

    public EventListenerMeterAspect(EventListenerMeter eventListenerMeter) {
        this.eventListenerMeter = eventListenerMeter;
    }

    @Pointcut("@annotation(com.bxm.warcar.integration.eventbus.core.Subscribe)")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object target = point.getTarget();
        if (!(target instanceof EventListener)) {
            return point.proceed();
        }

        EventListener eventListener = (EventListener) target;
        Timer timer = eventListenerMeter.getTimer(eventListener);
        long start = System.nanoTime();
        try {
            return point.proceed();
        } finally {
            if (Objects.nonNull(timer)) {
                timer.record((System.nanoTime() - start), TimeUnit.NANOSECONDS);
            }
        }
    }
}
