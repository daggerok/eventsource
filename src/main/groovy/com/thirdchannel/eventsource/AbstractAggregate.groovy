package com.thirdchannel.eventsource

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@EqualsAndHashCode
@CompileStatic
@ToString
abstract class AbstractAggregate extends AbstractFunctionalAggregate {

    UUID id = UUID.randomUUID()
    int revision = 0

    List<Event> uncommittedEvents = []
}
