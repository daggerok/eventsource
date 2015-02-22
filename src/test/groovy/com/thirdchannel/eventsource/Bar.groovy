package com.thirdchannel.eventsource

import groovy.transform.CompileStatic

/**
 * @author Steve Pember
 */
@CompileStatic
class Bar extends AbstractAggregate {

    UUID id = UUID.randomUUID()
    String aggregateDescription
    int revision = 0

    private List<Event> uncommittedEvents = []

    List<Event> getUncommittedEvents() {
        uncommittedEvents
    }

    void setUncommittedEvents(List<Event> events) {
        uncommittedEvents = events
    }

    String name
    int count
}
