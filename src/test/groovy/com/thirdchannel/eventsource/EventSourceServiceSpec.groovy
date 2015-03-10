package com.thirdchannel.eventsource

import spock.lang.Specification

/**
 * @author Steve Pember
 */
class EventSourceServiceSpec extends Specification {

    private EventSourceService eventSourceService = new EventSourceService()

    void "#save should return false if the underlying aggregateService fails"() {
        given:
            eventSourceService.aggregateService = [save: { Aggregate a, int r, List<Event> e -> false }] as AggregateService

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
            eventSourceService.aggregateService = [save: { Aggregate a, int r, List<Event> e -> true }] as AggregateService

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
            foo2.revision == 0
            foo3.revision == 0

        when:
            boolean result = eventSourceService.save(bar)

        then:
            result
            bar.revision == 3
            foo1.revision == 1
            foo2.revision == 2
            foo3.revision == 3
    }

    void "Loading Historical events should only be applied once" () {

    }
}
