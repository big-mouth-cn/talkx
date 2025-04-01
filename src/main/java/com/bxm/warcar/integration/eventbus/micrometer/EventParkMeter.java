package com.bxm.warcar.integration.eventbus.micrometer;

import com.bxm.warcar.integration.eventbus.AsyncEventPark;
import com.bxm.warcar.integration.eventbus.EventPark;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * @author allen
 * @date 2019/6/20
 * @since 1.0.0
 */
public class EventParkMeter implements MeterBinder {

    private final Iterable<EventPark> parks;

    public EventParkMeter(Iterable<EventPark> parks) {
        this.parks = parks;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (EventPark park : parks) {
            if (park instanceof AsyncEventPark) {
                // event park of async
                AsyncEventPark asyncEventPark = (AsyncEventPark) park;

                Gauge.builder("warcar.integration.eventbus.park.queue", 0, value -> asyncEventPark.getQueueSize()).register(registry);
                Gauge.builder("warcar.integration.eventbus.park.core", 0, value -> asyncEventPark.getCorePoolSize()).register(registry);
                Gauge.builder("warcar.integration.eventbus.park.active", 0, value -> asyncEventPark.getActiveCount()).register(registry);
            }
        }
    }
}
