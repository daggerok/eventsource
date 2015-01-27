package com.thirdchannel.eventsource

import groovy.transform.CompileStatic

/**
 * @author Steve Pember
 */
@CompileStatic
class FooEvent extends AbstractEvent {
    transient String name
    transient int count

    @Override
    void restoreData(final Map data) {
        name = data.name.toString()
        count = (int)data.count
    }

    @Override
    void process(Aggregate root) {
        Bar bar = (Bar)root
        bar.count = Integer.parseInt(count.toString())
        bar.name = name.toString()

    }
}
