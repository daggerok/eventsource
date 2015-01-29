package com.thirdchannel.eventsource

import spock.lang.Specification

/**
 * @author Steve Pember
 */
class EventSourceServiceSpec extends Specification {

    EventSourceService eventSourceService = new EventSourceService()

    def setup() {
    }

    void "#save should return false if the underlying aggregateService fails"() {
        given:
        eventSourceService.aggregateService = new AggregateService() {
            EventService eventService

            @Override
            Aggregate get(UUID id) {
                null
            }

            @Override
            List<Aggregate> getAll(List<UUID> ids) {
                return null
            }

            @Override
            Aggregate getOrCreate(UUID id, String aggregateDescription) {
                return null
            }

            @Override
            Boolean exists(UUID aggregateId) {
                return null
            }

            @Override
            Integer getCurrentRevision(UUID id) {
                return null
            }

            @Override
            Boolean save(Aggregate aggregate, Integer expectedRevision, List<Event> events) {
                false
            }
        }

        Bar bar = new Bar(aggregateDescription: "Bar Root")
        Event foo = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 1, name: "Test")
        bar.applyChange(foo)

        when:
        Boolean result = eventSourceService.save(bar)

        then:
        !result
    }

    void "#save should return true and update revisions upon a good persist"() {
        given:
        eventSourceService.aggregateService = new AggregateService() {
            EventService eventService

            @Override
            Aggregate get(UUID id) {
                null
            }

            @Override
            List<Aggregate> getAll(List<UUID> ids) {
                return null
            }

            @Override
            Aggregate getOrCreate(UUID id, String aggregateDescription) {
                return null
            }

            @Override
            Boolean exists(UUID aggregateId) {
                return null
            }

            @Override
            Integer getCurrentRevision(UUID id) {
                return null
            }

            @Override
            Boolean save(Aggregate aggregate, Integer expectedRevision, List<Event> events) {
                true
            }
        }
        Bar bar = new Bar(aggregateDescription: "Bar Root")
        Event foo1 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 5, name: "Test")
        Event foo2 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 10000, name: "Test3")
        Event foo3 = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "", count: 25, name: "Test2")

        bar.applyChange(foo1)
        bar.applyChange(foo2)
        bar.applyChange(foo3)
        // at this point, the revisions have not yet been updated
        assert bar.revision == 0
        assert foo1.revision == 0
        assert foo2.revision == 0
        assert foo3.revision == 0

        when:
        Boolean result = eventSourceService.save(bar)

        then:
        result
        bar.revision == 3
        foo1.revision == 1
        foo2.revision == 2
        foo3.revision == 3


    }

}
