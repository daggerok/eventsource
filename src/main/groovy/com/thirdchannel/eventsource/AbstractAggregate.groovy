package com.thirdchannel.eventsource

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@CompileStatic
@ToString
class AbstractAggregate implements Aggregate {

    UUID id = UUID.randomUUID()
    String aggregateDescription
    int revision = 0

    private final List<Event> uncommittedEvents = []

    @Override
    List<Event> getUncommittedEvents() {
        uncommittedEvents
    }

    @Override
    void markEventsAsCommitted() {
        uncommittedEvents.clear()
    }

    @Override
    void loadFromPastEvents(List<Event> events) {
        for(Event event : events) {
            runEvent(event, false)
        }
    }

    @Override
    void applyChange(Event event) {
        runEvent(event, true)
    }

    private void runEvent(Event event, boolean newEvent) {
        event.process this
        if (newEvent) {
            uncommittedEvents.add event
        }
    }
}
