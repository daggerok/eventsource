package com.thirdchannel.eventsource.aggregates

import com.thirdchannel.eventsource.AbstractAggregate
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
