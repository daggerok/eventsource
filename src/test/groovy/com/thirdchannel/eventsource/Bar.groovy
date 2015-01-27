package com.thirdchannel.eventsource

import com.thirdchannel.eventsource.AbstractAggregate
import groovy.transform.CompileStatic

/**
 * @author Steve Pember
 */
@CompileStatic
class Bar extends AbstractAggregate {

    String name
    int count = 0

}
