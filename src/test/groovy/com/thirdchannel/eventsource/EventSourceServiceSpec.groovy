package com.thirdchannel.eventsource

import com.thirdchannel.eventsource.aggregates.Bar
import com.thirdchannel.eventsource.events.FooEvent
import com.thirdchannel.eventsource.mock.BarAggregateService
import com.thirdchannel.eventsource.mock.MockEventService
import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * @author Steve Pember
 */
@Slf4j
class EventSourceServiceSpec extends Specification {

    private EventSourceService eventSourceService

    def setup() {
        eventSourceService = new EventSourceService<Bar>()
        eventSourceService.aggregateService = new BarAggregateService()
        eventSourceService.eventService = new MockEventService()
    }

    void "#save should return true and update revisions upon a good persist"() {

        when:
            Bar bar = new Bar()
            Event foo1 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 5, name: "Test")
            Date oldDate = new Date()-5
            Event foo2 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 10000, name: "Test3", dateEffective: oldDate)
            Event foo3 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 25, name: "Test2")

            bar.applyChange(foo1)
            bar.applyChange(foo2)
            bar.applyChange(foo3)

            // at this point, the revisions have not yet been updated
            then:
            bar.revision == 0
            foo1.revision == 0
            foo1.dateEffective == foo1.date
            foo1.date != null
            foo1.data == ""
            foo2.revision == 0
            foo2.dateEffective == oldDate
            foo2.date != oldDate
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

    void "#save should return true and update revisions for multiple aggregates" () {
        given:
            Bar bar = new Bar()
            List<Event> barEvents = [new FooEvent(aggregateId: bar.id, count: 5, name: "test"),
                                     new FooEvent(aggregateId: bar.id, count: 10, name: "test"),
                                     new FooEvent(aggregateId: bar.id, count:7, name: "Blah")
            ]
            Bar b2 = new Bar()
            List<Event> b2Events = [new FooEvent(aggregateId: b2.id, count: 100, name: "test"),
                                     new FooEvent(aggregateId: b2.id, count: 5, name: "test"),
                                     new FooEvent(aggregateId: b2.id, count:75, name: "Blah2")
            ]

        when:
            barEvents.each {bar.applyChange(it)}
            b2Events.each {b2.applyChange(it)}
            boolean result = eventSourceService.save([bar, b2])

        then:
            result
            bar.revision == 3
            b2.revision == 3
            bar.count == 22
            bar.name == "Blah"
            b2.count == 180
            b2.name == "Blah2"
    }

    void "Retrieving the current state of an Aggregate build up from the events" () {
        given:
            Bar bar = new Bar(id: UUID.randomUUID())

            Event foo1 = new FooEvent(aggregateId: bar.id, userId: "1", data: "", count: 5, name: "Test")
            Event foo2 = new FooEvent(aggregateId: bar.id, userId: "1", data: "", count: 100, name: "Test3")
            Event foo3 = new FooEvent(aggregateId: bar.id, userId: "1", data: "", count: 25, name: "Test2")
            bar.applyChanges([foo1, foo2, foo3])
            eventSourceService.save(bar)

        when:
            Bar check = (Bar)eventSourceService.get(bar.id)
            eventSourceService.loadCurrentState(check)

        then:
            check.count == 130
            check.name == "Test2"
    }

    void "Retrieving current state of multiple aggregates should build up from their respective events" () {
        given:
            Bar bar = new Bar(id: UUID.randomUUID())
            Event foo1 = new FooEvent(aggregateId: bar.id, userId: "1", data: "", count: 5, name: "Test")
            Event foo2 = new FooEvent(aggregateId: bar.id, userId: "1", data: "", count: 100, name: "Test3")
            Event foo3 = new FooEvent(aggregateId: bar.id, userId: "1", data: "", count: 25, name: "Test2")
            bar.applyChanges([foo1, foo2, foo3])
            eventSourceService.save(bar)

            Bar bar2 = new Bar(id: UUID.randomUUID())
            Event foo4 = new FooEvent(aggregateId: bar2.id, userId: "1", data: "", count: 10, name: "Baz")
            Event foo5 = new FooEvent(aggregateId: bar2.id, userId: "1", data: "", count: 999, name: "Baz3")
            Event foo6 = new FooEvent(aggregateId: bar2.id, userId: "1", data: "", count: 1, name: "Baz2")
            bar2.applyChanges([foo4, foo5, foo6])
            eventSourceService.save(bar2)



        when:
            Bar check = (Bar)eventSourceService.get(bar.id)
            Bar check2 = (Bar)eventSourceService.get(bar2.id)
            eventSourceService.loadCurrentState([check, check2])
        then:
            check.count == 130
            check.name == "Test2"
            check2.count == 1010
            check2.name == "Baz2"

    }
}
