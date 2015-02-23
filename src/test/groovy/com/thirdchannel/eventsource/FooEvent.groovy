package com.thirdchannel.eventsource

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@ToString
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
