package com.thirdchannel.eventsource

import groovy.transform.CompileStatic

/**
 * An additional layer between AbstractAggregate and Aggregate, allowing clients to preserve functionality if they don't
 * want / need the defined properties of AbstractAggregate.
 *
 * Specifically, this came out of using JPA with AbstractAggregate.
 *
 * @author Steve Pember
 */
@CompileStatic
abstract class AbstractFunctionalAggregate implements Aggregate {

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
        event.process this
        if (newEvent) {
            //mark 'ownership' of the event the moment it's run, if new
            event.aggregateId = id
            uncommittedEvents << event
        }
    }
}
