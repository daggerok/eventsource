package com.thirdchannel.eventsource

import com.thirdchannel.eventsource.aggregates.Bar
import com.thirdchannel.eventsource.events.FooEvent
import spock.lang.Specification

/**
 * @author Steve Pember
 */
class AbstractAggregateSpec extends Specification {

    void "Applying an event should change the aggregate and move the event into uncommitted events"() {
        when:
            Bar bar = new Bar()
            Event foo = new FooEvent(revision: 0, aggregateId: bar.id, userId: "1", data: "{'count':1,'name':'test'}")

        then:
            bar.count == 0
            !bar.name

        when:
            // the eventSourceService should be responsible for hydrating the event data
            foo.restoreData([count:1, name:"test"])
            bar.applyChange(foo)


        then:
            bar.uncommittedEvents.size() == 1
            bar.uncommittedEvents[0] == foo
            bar.count == 1
            bar.name == "test"
    }

    void "Restoring an event from history should change the aggregate but not have the event listed as uncommitted"() {
        given:
            Bar bar = new Bar()
            Event foo = new FooEvent(revision:0, aggregateId: bar.id, userId: "1", data: "")

        when:
            foo.restoreData([count:5, name: "Snow Day"])
            bar.loadFromPastEvents([foo])

        then:
            !bar.uncommittedEvents

            bar.count == 5
            bar.name == "Snow Day"
    }

    void "Marking an aggregate's events as committed should clear the list"() {

        when:
            UUID custom = UUID.randomUUID()
            Bar bar = new Bar( id: custom)

        then:
            bar.id == custom

        when:
            Event foo = new FooEvent(revision:0, aggregateId: bar.id, userId: "1", data: "")
            Event fop = new FooEvent(revision:1, aggregateId: bar.id, userId: "1", data: "")
            foo.restoreData([count:1, name: "Test"])
            fop.restoreData([count:2, name: "Whizbang"])
            bar.applyChange(foo)
            bar.applyChange(fop)

        then:
            bar.count == 3
            bar.name == "Whizbang"
            bar.uncommittedEvents.size() == 2

        when:
            bar.markEventsAsCommitted()

        then:
            !bar.uncommittedEvents
    }

}
