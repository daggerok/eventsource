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

    void setRevision(final int r) {
        // allow us to set the revision the first time
        if (!this.revision) {
            this.revision = r
        } else {
            throw new ReadOnlyPropertyException("revision", getClazz())
        }
    }
}
