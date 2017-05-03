package com.thirdchannel.eventsource.mock

import com.thirdchannel.eventsource.Aggregate
import com.thirdchannel.eventsource.Event
import com.thirdchannel.eventsource.EventService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * @author Steve Pember
 */
@Slf4j
@CompileStatic
class MockEventService implements EventService {

    Map<UUID, List<Event>> database = [:]

    @Override
    List<Event> findAllEventsForAggregate(Aggregate aggregate) {
        if (database.containsKey(aggregate.id)) {
            database[aggregate.id].sort(new EventDateEffectiveComparator())
            database[aggregate.id]
        } else {
            []
        }
    }

    @Override
    List<Event> findAllEventsForAggregates(List<? extends Aggregate> aggregates) {
        List<Event> events = []
        aggregates.each {events.addAll(findAllEventsForAggregate(it))}
        events
    }

    @Override
    List<Event> findAllEventsForAggregateInRange(Aggregate aggregate, Date begin, Date end) {
        return null
    }

    @Override
    List<Event> findAllEventsForAggregatesInRange(List<? extends Aggregate> aggregate, Date begin, Date end) {
        return null
    }

    @Override
    List<Event> findAllEventsForAggregateUpToRevision(Aggregate aggregate, int revision) {
        return null
    }

    @Override
    List<Event> findAllEventsForAggregateUpToDateEffective(Aggregate aggregate, Date date) {
        List<Event> events = findAllEventsForAggregate(aggregate)
        // should be sorted by date effective!
        events.findAll {Event event -> event.dateEffective <= date}.toList()
    }

    @Override
    boolean save(Event event) {
        save([event])
    }

    @Override
    boolean save(List<? extends Event> events) {
        events.each { Event e ->
            if (!database.containsKey(e.aggregateId)) {
                database[e.aggregateId] = []
            }
            database[e.aggregateId].add(e)
        }

        true
    }
}

class EventDateEffectiveComparator implements Comparator<Event> {
    @Override
    int compare(Event o1, Event o2) {
        return o1.dateEffective <=> o2.dateEffective
    }
}
