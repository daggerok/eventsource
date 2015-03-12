package com.thirdchannel.eventsource

import com.thirdchannel.eventsource.annotation.EventData
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@ToString
@CompileStatic
class FooEvent extends AbstractEvent {

    @EventData
    String name

    @EventData int count = 0

    void restoreData(final Map data) {
        name = data.name
        count = (int)data.count
    }

    void process(Aggregate root) {
        Bar bar = (Bar)root
        bar.count += count
        bar.name = name
    }
}
