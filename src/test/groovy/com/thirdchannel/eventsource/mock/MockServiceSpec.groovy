package com.thirdchannel.eventsource.mock

import com.thirdchannel.eventsource.AggregateService
import com.thirdchannel.eventsource.Event
import com.thirdchannel.eventsource.EventService
import com.thirdchannel.eventsource.aggregates.Bar
import com.thirdchannel.eventsource.events.FooEvent
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Steve Pember
 */
@Slf4j
@Subject([BarAggregateService, MockEventService])
class MockServiceSpec extends Specification {


    AggregateService<Bar> aggregateService
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
            List<FooEvent> events = eventService.findAllEventsForAggregate(bar).collect { Event event -> (FooEvent) event }
        then:
            events.size() == 3
            events.get(0).name == "first"
            events.get(1).name == "third"
            events.get(2).name == "second"
    }

    void "Attempting to get an aggregate from the aggregate service should return an optional that indicates the aggregate's existence"() {
        when:
        Bar bar = new Bar()
        aggregateService.save(bar)
        Optional<Bar> existing = aggregateService.get(bar.id)
        Optional<Bar> notExisting = aggregateService.get(UUID.randomUUID())
        then:
        existing.isPresent()
        existing.get().id == bar.id
        !notExisting.isPresent()
    }

}
