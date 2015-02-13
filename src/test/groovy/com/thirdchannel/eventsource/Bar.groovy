package com.thirdchannel.eventsource

import groovy.transform.CompileStatic

/**
 * @author Steve Pember
 */
@CompileStatic
class Bar extends AbstractAggregate {
    String name
    int count
}
