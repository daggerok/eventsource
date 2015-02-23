package com.thirdchannel.eventsource

import groovy.transform.CompileStatic

/**
 * @author Steve Pember
 */
@CompileStatic
class Bar extends AbstractAggregate {

    UUID id = UUID.randomUUID()
    String aggregateDescription
    int revision
    List<Event> uncommittedEvents = []

    String name
    int count
}
