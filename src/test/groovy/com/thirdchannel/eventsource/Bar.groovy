package com.thirdchannel.eventsource

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Steve Pember
 */
@CompileStatic
@ToString
class Bar extends AbstractAggregate {
    String name
    int count
}
