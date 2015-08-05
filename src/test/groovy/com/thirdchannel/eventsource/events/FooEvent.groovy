package com.thirdchannel.eventsource.events

import com.thirdchannel.eventsource.AbstractEvent
import com.thirdchannel.eventsource.Aggregate
import com.thirdchannel.eventsource.aggregates.Bar
import com.thirdchannel.eventsource.annotation.EventData
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@ToString
@CompileStatic
class FooEvent extends AbstractEvent<Bar> {

    @EventData
    String name

    @EventData
    int count = 0

    void restoreData(final Map data) {
        name = data.name
        count = (int)data.count
    }

    @Override
    void process(Bar aggregate) {
        aggregate.count += count
        aggregate.name = name
    }
}
