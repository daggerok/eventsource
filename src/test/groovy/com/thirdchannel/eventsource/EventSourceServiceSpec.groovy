package com.thirdchannel.eventsource

import com.thirdchannel.eventsource.aggregates.Bar
import com.thirdchannel.eventsource.events.FooEvent
import com.thirdchannel.eventsource.mock.BarAggregateService
import com.thirdchannel.eventsource.mock.MockEventService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * @author Steve Pember
 */
@Slf4j
class EventSourceServiceSpec extends Specification {

    private EventSourceService<Bar> eventSourceService

    private createFooEvent(int count, String name, Date dateEffective=null) {
        new FooEvent(count: count, name: name, userId: "test@test.com", data:"", dateEffective: dateEffective)
    }

    def setup() {
        eventSourceService = new EventSourceService<Bar>()
        eventSourceService.aggregateService = new BarAggregateService()
        eventSourceService.eventService = new MockEventService()
    }

    void "#save should return true and update revisions upon a good persist"() {

        when:
            Bar bar = new Bar()
            Event foo1 = createFooEvent(5, "Test")
            Date oldDate = new Date()-5
            Event foo2 = createFooEvent(10000, "Test 3", oldDate)
            Event foo3 = createFooEvent(25, "Test2")

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

    void "#save should return false when events save fails"() {
        given:
            MockEventService eventService = Mock(MockEventService)

            eventService.save(_) >> false

            EventSourceService<Bar> eventSourceService = new EventSourceService<Bar>()
            eventSourceService.aggregateService = new BarAggregateService()
            eventSourceService.eventService = eventService

            Bar bar = new Bar()
            Event foo1 = createFooEvent(5, "Test")
            Date oldDate = new Date()-5
            Event foo2 = createFooEvent(10000, "Test 3", oldDate)
            Event foo3 = createFooEvent(25, "Test2")

            bar.applyChange(foo1)
            bar.applyChange(foo2)
            bar.applyChange(foo3)

        when:
            boolean result = eventSourceService.save(bar)

        then:
            !result
            bar.uncommittedEvents.size() == 3
    }

    void "#save should return true and update revisions for multiple aggregates" () {
        given:
            Bar bar = new Bar()
            List<Event> barEvents = [createFooEvent(5, "Test"), createFooEvent(10, "test"), createFooEvent(7, "Blah")]

            Bar b2 = new Bar()
            List<Event> b2Events = [createFooEvent(100, "test"), createFooEvent(5, "test"), createFooEvent(75, "Blah2")]

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
            Event foo1 = createFooEvent(5, "Test")
            Event foo2 = createFooEvent(100, "Test3")
            Event foo3 = createFooEvent(25, "Test2")
            bar.applyChanges([foo1, foo2, foo3])
            eventSourceService.save(bar)

        when:
            Bar check = eventSourceService.get(bar.id).get()
            eventSourceService.loadCurrentState(check)

        then:
            check.count == 130
            check.name == "Test2"
    }

    void "Retrieving current state of multiple aggregates should build up from their respective events" () {
        given:
            Bar bar = new Bar(id: UUID.randomUUID())
            Event foo1 = createFooEvent(5, "Test")
            Event foo2 = createFooEvent(100, "Test3")
            Event foo3 = createFooEvent(25, "Test2")
            bar.applyChanges([foo1, foo2, foo3])
            eventSourceService.save(bar)

            Bar bar2 = new Bar(id: UUID.randomUUID())
            Event foo4 = createFooEvent(10, "Baz")
            Event foo5 = createFooEvent(999, "Baz3")
            Event foo6 = createFooEvent(1, "Baz2")
            bar2.applyChanges([foo4, foo5, foo6])
            eventSourceService.save(bar2)



        when:
            Bar check = eventSourceService.get(bar.id).get()
            Bar check2 = eventSourceService.get(bar2.id).get()
            eventSourceService.loadCurrentState([check, check2])
        then:
            check.count == 130
            check.name == "Test2"
            check2.count == 1010
            check2.name == "Baz2"
    }

    void "Loading up to a past date should restore an Aggregate to the state up to that date" () {
        given:
            Bar bar = new Bar()
            bar.applyChanges([
                    createFooEvent(5, "a", new Date()-30),
                    createFooEvent(5, "ab", new Date()-25),
                    createFooEvent(5, "abc", new Date()-20),
                    createFooEvent(5, "abcd", new Date()-10),
                    createFooEvent(5, "abcde", new Date()-5),
                    createFooEvent(5, "abcdef", new Date()),
            ])
            eventSourceService.save(bar)

        when:
            Bar check = eventSourceService.get(bar.id).get()
            eventSourceService.loadHistoryUpTo(check, new Date()-8)
        then:
            bar.count == 30
            bar.name == "abcdef"

            check.count == 20
            check.name == "abcd"
    }

    void "Attempting to get an aggregate should return an optional that indicates the aggregate's existence"() {
        when:
        Bar bar = new Bar()
        eventSourceService.save(bar)
        Optional<Bar> existing = eventSourceService.get(bar.id)
        Optional<Bar> notExisting = eventSourceService.get(UUID.randomUUID())
        then:
        existing.isPresent()
        existing.get().id == bar.id
        !notExisting.isPresent()
    }
}
