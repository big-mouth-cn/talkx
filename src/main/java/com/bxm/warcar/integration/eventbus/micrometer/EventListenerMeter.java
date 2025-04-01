package com.bxm.warcar.integration.eventbus.micrometer;

import com.bxm.warcar.integration.eventbus.EventListener;
import com.bxm.warcar.integration.eventbus.core.Subscribe;
import com.google.common.collect.Maps;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodIntrospector;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * @author allen
 * @date 2019/6/20
 * @since 1.0.0
 */
public class EventListenerMeter implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerMeter.class);

    private final Map<Class, Timer> timer = Maps.newHashMap();
    private final Iterable<EventListener> eventListeners;

    public EventListenerMeter(Iterable<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (EventListener eventListener : eventListeners) {
            Class<?> targetClass = ClassUtils.getUserClass(eventListener.getClass());

            Set<Method> methods = MethodIntrospector.selectMethods(targetClass, new ReflectionUtils.MethodFilter() {
                @Override
                public boolean matches(Method method) {
                    return method.isAnnotationPresent(Subscribe.class);
                }
            });

            for (Method method : methods) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (ArrayUtils.isEmpty(parameterTypes)) {
                    continue;
                }
                if (parameterTypes.length > 1) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Method {} number of parameters must be 1", method);
                    }
                }
                Timer timer = Timer.builder("warcar.integration.eventbus.listener")
                        .tags("name", targetClass.getName())
                        .tags("type", parameterTypes[0].getSimpleName())
                        .register(registry);
                this.timer.put(targetClass, timer);
            }

        }
    }

    Timer getTimer(EventListener eventListener) {
        return timer.get(ClassUtils.getUserClass(eventListener.getClass()));
    }
}
