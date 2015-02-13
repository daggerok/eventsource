package com.thirdchannel.eventsource

import groovy.transform.CompileStatic

/**
 * @author Steve Pember
 */
@CompileStatic
class FooEvent extends AbstractEvent {
    transient String name
    transient int count

    void restoreData(final Map data) {
        name = data.name
        count = (int)data.count
    }

    void process(Aggregate root) {
        Bar bar = (Bar)root
        bar.count = count
        bar.name = name
    }
}
