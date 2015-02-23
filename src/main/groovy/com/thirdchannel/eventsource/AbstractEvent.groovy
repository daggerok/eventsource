package com.thirdchannel.eventsource

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@EqualsAndHashCode
@ToString
@CompileStatic
abstract class AbstractEvent implements Event {

    UUID id = UUID.randomUUID()
    int revision
    UUID aggregateId
    Date date = new Date()
    String data
    String userId
    String clazz = this.class.name

    void setRevision(final int r) {
        // allow us to set the revision the first time
        if (!revision) {
            revision = r
        } else {
            throw new ReadOnlyPropertyException("revision", clazz)
        }
    }
}
