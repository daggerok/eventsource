package com.thirdchannel.eventsource

import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * @author Steve Pember
 */
@Slf4j
class EventSourceServiceSpec extends Specification {

    private EventSourceService eventSourceService = new EventSourceService()

    void "#save should return false if the underlying aggregateService fails"() {
        given:
            eventSourceService.aggregateService = [save: { Aggregate a, int r -> 0 }, exists: {UUID id -> true}, update: { Aggregate a, int r -> 0 }] as AggregateService

            Bar bar = new Bar(aggregateDescription: "Bar Root")
            Event foo = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 1, name: "Test")
            bar.applyChange(foo)

        when:
            boolean result = eventSourceService.save(bar)

        then:
            !result
    }

    void "#save should return true and update revisions upon a good persist"() {
        given:
            eventSourceService.aggregateService = [save: { Aggregate a, int r -> 0 }, exists: {UUID id -> true}, update: { Aggregate a, int r -> 1 }] as AggregateService
            eventSourceService.eventService = [save: { List<Event> e -> true }] as EventService

        when:
            Bar bar = new Bar(aggregateDescription: "Bar Root")
            Event foo1 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 5, name: "Test")
            Event foo2 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 10000, name: "Test3")
            Event foo3 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 25, name: "Test2")

            bar.applyChange(foo1)
            bar.applyChange(foo2)
            bar.applyChange(foo3)

            // at this point, the revisions have not yet been updated
            then:
            bar.revision == 0
            foo1.revision == 0
            foo1.data == ""
            foo2.revision == 0
            foo3.revision == 0



        when:
            boolean result = eventSourceService.save(bar)

        then:
            result
            bar.revision == 3
            foo1.revision == 1
            foo1.data == '{"name":"Test","count":5}'
            foo2.revision == 2
            foo3.revision == 3


    }

    void "Retrieving the current state of an Aggregate build up from the events" () {
        given:
        Bar bar = new Bar(aggregateDescription: "Bar Root")
        Event foo1 = new FooEvent(revision: 1, aggregateId: bar.id, userId: "1", data: "", count: 5, name: "Test")
        Event foo2 = new FooEvent(revision: 2, aggregateId: bar.id, userId: "1", data: "", count: 100, name: "Test3")
        Event foo3 = new FooEvent(revision: 3, aggregateId: bar.id, userId: "1", data: "", count: 25, name: "Test2")
        eventSourceService.serializeEvents([foo1, foo2, foo3])
        eventSourceService.eventService = [findAllEventsForAggregate: { [foo1,foo2,foo3] }] as EventService

        when:
            eventSourceService.loadCurrentState(bar)

        then:
            bar.count == 130
            bar.name == "Test2"
    }

    void "Retrieving current state of multiple aggregates should build up from their respective events" () {
        given:
        Bar bar = new Bar(aggregateDescription: "Bar Root")
        Event foo1 = new FooEvent(revision: 1, aggregateId: bar.id, userId: "1", data: "", count: 5, name: "Test")
        Event foo2 = new FooEvent(revision: 2, aggregateId: bar.id, userId: "1", data: "", count: 100, name: "Test3")
        Event foo3 = new FooEvent(revision: 3, aggregateId: bar.id, userId: "1", data: "", count: 25, name: "Test2")
        eventSourceService.serializeEvents([foo1, foo2, foo3])

        Bar bar2 = new Bar(aggregateDescription: "Second Bar root")
        Event foo4 = new FooEvent(revision: 1, aggregateId: bar2.id, userId: "1", data: "", count: 10, name: "Baz")
        Event foo5 = new FooEvent(revision: 2, aggregateId: bar2.id, userId: "1", data: "", count: 999, name: "Baz3")
        Event foo6 = new FooEvent(revision: 3, aggregateId: bar2.id, userId: "1", data: "", count: 1, name: "Baz2")
        eventSourceService.serializeEvents([foo4, foo5, foo6])

        eventSourceService.eventService = [findAllEventsForAggregates: { [foo1,foo4, foo2,foo5, foo3, foo6] }] as EventService

        when:
            eventSourceService.loadCurrentState([bar, bar2])
        then:
        bar.count == 130
        bar.name == "Test2"
        bar2.count == 1010
        bar2.name == "Baz2"

    }
}
