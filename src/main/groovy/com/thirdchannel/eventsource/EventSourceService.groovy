package com.thirdchannel.eventsource

import com.thirdchannel.eventsource.annotation.EventData
import com.thirdchannel.eventsource.serialize.EventSerializer
import com.thirdchannel.eventsource.serialize.JsonBuilderEventSerializer
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import rx.functions.Func1
import rx.observables.GroupedObservable

import java.lang.reflect.Field
import java.util.function.Function

/**
 * EventSourceService is the main entry point through which all interactions with EventSourcing should occur.
 * The goal is for clients to avoid working with the underlying services directly.
 *
 * @author steve pember
 */
@Slf4j
@CompileStatic
class EventSourceService<A extends Aggregate> {

    AggregateService aggregateService

    EventService eventService

    //todo: SnapshotService snapshotService

    protected EventSerializer eventSerializer = new JsonBuilderEventSerializer()

    /**
     * Use to Override the default Event Serialization
     *
     * @param es
     */
    void setEventSerializer(EventSerializer es) {
        eventSerializer = es
    }

    // find or create aggregate
    A get(UUID aggregateId) {
        aggregateService.get(aggregateId)
    }

    A getCurrent(UUID aggregateId) {
        A aggregate = get(aggregateId)
        loadCurrentState(aggregate)
        aggregate
    }


    List<A> getAll(List<UUID> aggregateIds) {
        aggregateService.getAll(aggregateIds)
    }

    List<A> getAllCurrent(List<UUID> aggregateIds) {
        List<A> aggregates = getAll(aggregateIds)
        loadCurrentState(aggregates)
        aggregates
    }

    A getOrCreate(UUID aggregateId) {
        aggregateService.getOrCreate(aggregateId)
    }


    /**
     * Saves an aggregate and its uncommitted events. Applies revision updates to the
     *
     * Should be wrapped in a Transactional block if available!
     *
     */
    boolean save(A aggregate) {
        save([aggregate])
    }

    boolean save(List<A> aggregates) {
        // save Uncommitted events. For each uncommitted event,increment the revision on the aggregate and set the
        // revision on the event.
        // pass to the aggregateService for persisting, with the idea that it will save both the Aggregate and events
        // within a Transaction, if possible

        int aggregateCount = aggregates.size()
        boolean result = false

        rx.Observable.from(aggregates)
        // this next does io!
        .filter({A aggregate -> 1 == saveAggregate(aggregate)})
        // at this point we should only those aggregates which were successful
        .map({A aggregate->
            serializeEvents(aggregate.uncommittedEvents)
            aggregate
        })
        .map({A aggregate->
            rx.Observable.from(aggregate.uncommittedEvents)
        } as Func1)
        .flatMap({it})
        .buffer(500)
        .subscribe({List<? extends Event> events->
            eventService.save(events)
        }, {
            log.error("Could not save: ", it )
        }, {
            aggregates.each {
                ((A)it).markEventsAsCommitted()
            }
            result = true
            }
        )
        result
    }

    /**
     *
     * @param aggregate
     * @param expectedRevision
     * @return
     */
    protected int saveAggregate(A aggregate) {
        int oldRevision = aggregate.revision
        // courtesy of burt:
        // update the aggregate revision and set the event equal to that new revision
        aggregate.uncommittedEvents.each { event-> event.revision = ++aggregate.revision }
        if (oldRevision == 0) {
            return aggregateService.save(aggregate)
        } else {
            return aggregateService.update(aggregate, oldRevision)
        }
    }

    /**
     *
     * @param events
     */
    protected void serializeEvents(List<? extends Event> events) {
        events.each {Event event->
            event.data = eventSerializer.serialize(event)
        }
    }

    /*
        The majority of these methods are simply pass-throughs, with the purpose of keeping a common barrier behind
        EventSourceService
     */

    void loadCurrentState(A aggregate) {
        // todo: add snapshot behavior
        loadHistoricalEventsForAggregates([aggregate], eventService.findAllEventsForAggregate(aggregate))

    }

    void loadCurrentState(List<A> aggregates) {
        loadHistoricalEventsForAggregates(aggregates, eventService.findAllEventsForAggregates(aggregates))
    }

    void loadHistoryUpTo(A aggregate, int targetRevision) {
        loadHistoricalEventsForAggregates([aggregate], eventService.findAllEventsForAggregateUpToRevision(aggregate, targetRevision))
    }

    void loadHistoryUpTo(A aggregate, Date targetDate) {
        loadHistoricalEventsForAggregates([aggregate], eventService.findAllEventsForAggregateUpToDateEffective(aggregate, targetDate))
    }

    void loadHistoryInRange(A aggregate, Date begin, Date end) {
        loadHistoricalEventsForAggregates([aggregate], eventService.findAllEventsForAggregateInRange(aggregate, begin, end))
    }

    void loadHistoryInRange(List<A> aggregates, Date begin, Date end) {
        List<Event> events = eventService.findAllEventsForAggregatesInRange(aggregates, begin, end)
        loadHistoricalEventsForAggregates(aggregates, events)
    }

    // current aggregate

    // process events -> check for revision, check for order of events

    // events are applied directly onto the aggregate.
    // The event store should be used to persist the uncomitted events on an aggregate, then increments the revision on
    // the aggregate

    // should be a method for loading events on an aggregate between a date range, on a specific date, all before / all after a date,
    // or all events. The method calls should use snapshot service


    // method for hydrating events for a single aggregate or multiple

    private void loadHistoricalEventsForAggregates(List<A> aggregates, List<Event> events) {

        Map<UUID, A> aggregateLookup = aggregates.collectEntries {A it ->[(it.id.toString()): it]}

        rx.Observable.from(events)
            .map({Event event->
                event.restoreData(eventSerializer.hydrate(event.data))
                event
            })
            .groupBy({((Event)it).aggregateId})
            .flatMap({GroupedObservable it->
                it.reduce([], {List l, item-> l += item})
                .map({List collectedEvents->
                    return ((A)aggregateLookup[it.key.toString()]).loadFromPastEvents(collectedEvents)}
                )
            })
            .subscribe({it}, {log.error("Unable to load events: ", it)}, {})
    }

}
