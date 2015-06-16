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
        } else {
            // by setting the revision on loading of historical events, we get two things:
            // 1. the aggregate revision matches its revision at that point in time (saving outside of current state should still fail)
            // 2. We can reduce the number of queries we make by not having to fetch the base aggregate to begin with (without this line, the revision would be 0 on the aggregate)
            revision = event.revision
        }
    }
}
