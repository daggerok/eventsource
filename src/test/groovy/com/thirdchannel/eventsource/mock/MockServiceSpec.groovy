package com.thirdchannel.eventsource.mock

import com.thirdchannel.eventsource.AggregateService
import com.thirdchannel.eventsource.Event
import com.thirdchannel.eventsource.EventService
import com.thirdchannel.eventsource.aggregates.Bar
import com.thirdchannel.eventsource.events.FooEvent
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Steve Pember
 */
@Slf4j
@Subject([BarAggregateService, MockEventService])
class MockServiceSpec extends Specification {


    AggregateService aggregateService
    EventService eventService

    def setup() {
        aggregateService = new BarAggregateService()
        eventService = new MockEventService()
    }

    void "MockEventService should return events sorted date effective"() {
        given:
            Bar bar = new Bar()
            assert bar.id != null
            log.info("Bar id = ${bar.id}")
            eventService.save([
                    new FooEvent(name: "first", revision: 1, dateEffective: new Date()-30, aggregateId: bar.id),
                    new FooEvent(name: "second", revision: 2, dateEffective: new Date()+40, aggregateId: bar.id),
                    new FooEvent(name: "third", revision: 3, dateEffective: new Date()-5, aggregateId: bar.id)
            ])
        when:
            List<? extends Event> events = eventService.findAllEventsForAggregate(bar)
        then:
            events.size() == 3
            events[0].name == "first"
            events[1].name == "third"
            events[2].name == "second"
    }


}
