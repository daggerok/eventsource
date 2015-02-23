package com.thirdchannel.eventsource

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@CompileStatic
@ToString
abstract class AbstractAggregate implements Aggregate {

    UUID id = UUID.randomUUID()
    String aggregateDescription
    int revision = 0

    List<Event> uncommittedEvents = []

    void markEventsAsCommitted() {
        uncommittedEvents.clear()
    }

    void loadFromPastEvents(List<Event> events) {
        for (Event event : events) {
            runEvent(event, false)
        }
    }

    void applyChange(Event event) {
        runEvent(event, true)
    }

    private void runEvent(Event event, boolean newEvent) {
        event.process this
        if (newEvent) {
            //mark 'ownership' of the event the moment it's run, if new
            event.aggregateId = this.id
            uncommittedEvents.add(event)
        }
    }
}
