package com.thirdchannel.eventsource

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@CompileStatic
@ToString
abstract class AbstractAggregate implements Aggregate {


    void markEventsAsCommitted() {
        uncommittedEvents.clear()
    }

    void loadFromPastEvents(List<Event> events) {
        events.each { runEvent it, false }
    }

    void applyChange(Event event) {
        runEvent(event, true)
    }

    private void runEvent(Event event, boolean newEvent) {
        //mark 'ownership' of the event the moment it's run
        event.aggregateId = id
        event.process this
        if (newEvent) {
            uncommittedEvents << event
        }
    }
}
