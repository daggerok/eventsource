package com.thirdchannel.eventsource

import com.thirdchannel.eventsource.annotation.EventData
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.lang.reflect.Field

/**
 * EventSourceService is the main entry point through which all interactions with EventSourcing should occur.
 * The goal is for clients to avoid working with the underlying services directly.
 *
 * @author steve pember
 */
@Slf4j
class EventSourceService {

    AggregateService aggregateService

    EventService eventService

    SnapshotService snapshotService

    // find or create aggregate
    Aggregate get(UUID aggregateId) {
        aggregateService.get(aggregateId)
    }

    List<Aggregate> getAll(List<UUID> aggregateIds) {
        aggregateService.getAll(aggregateIds)
    }

    Aggregate getOrCreate(UUID aggregateId, String description) {
        aggregateService.getOrCreate(aggregateId, description)
    }

    Aggregate create(String description) {
        aggregateService.create description
    }

//    Aggregate findOrCreateById(UUID aggregateId) {
//
//    }
    /**
     * Saves an aggregate and its uncommitted events. Applies revision updates to the
     *
     * Should be wrapped in a Transactional block if available!
     *
     */
    boolean save(Aggregate aggregate) {
        // save Uncommitted events. For each uncommitted event,increment the revision on the aggregate and set the
        // revision on the event.
        // pass to the aggregateService for persisting, with the idea that it will save both the Aggregate and events
        // within a Transaction, if possible
        int oldRevision = aggregate.revision
        // courtesy of burt:
        // update the aggregate revision and set the event equal to that new revision
        aggregate.uncommittedEvents.each { it.revision = ++aggregate.revision }

        // only proceed if we receive a 1 from the saveAggregate function. A zero implies that the save could not occur,
        // likely due to the aggregate being out of version. Anything more than a 1 implies disaster: more than 1 aggregate with that id!
        int rowsAffected = saveAggregate(aggregate, oldRevision)
        if (1 == rowsAffected) {
            // finally, mark the aggregate's changes as committed to 'flush' the events and prepare for more
            serializeEvents(aggregate.uncommittedEvents)
            if (eventService.save(aggregate.uncommittedEvents)) {
                log.debug("Uncommitted Events persisted. Clearing events from aggregate")
                aggregate.markEventsAsCommitted()
                true
            } else {
                log.error("EventService reporting that events failed to save.")
                false
            }
        } else {
            log.error("Error updating or saving aggregate. Received a row count updated of {} when it should be 1", rowsAffected)
            false
        }
    }

    /**
     *
     * @param aggregate
     * @param expectedRevision
     * @return
     */
    protected int saveAggregate(Aggregate aggregate, int expectedRevision) {
        aggregateService.exists(aggregate.id) ? aggregateService.update(aggregate, expectedRevision) : aggregateService.save(aggregate)
    }

    /**
     *
     * @param events
     */
    protected void serializeEvents(List<Event> events) {
        JsonBuilder builder = new JsonBuilder()
        for(Event event: events) {
            serializeEventData(event, builder)
            log.info("Event data is now {}", event.data)
        }


    }

    private void serializeEventData(Event event, JsonBuilder builder) {

        //rx.Observable.from(event.class.getDeclaredFields())
        rx.Observable.from(getAllFields(event.class))
            .filter({Field f -> f.isAnnotationPresent(EventData)})
            .reduce([:], {agg, f ->
                agg[f.getName()] = event.getProperties()[f.getName()]
                agg
            })
            .map({
                builder(it)
                builder.toString()
            })
            .subscribe(
                {event.data = it},
                {log.error("Failed", it)},
                {}
            )
    }

    private List<Field> getAllFields(Class<?> object) {
        List<Field> fields = []
        for (Class<?> c = object; c != null; c = c.getSuperclass()) {
            fields.addAll(c.getDeclaredFields());
        }
        fields
    }

    /*
        The majority of these methods are simply pass-throughs, with the purpose of keeping a common barrier behind
        EventSourceService
     */

    void loadCurrentState(Aggregate aggregate) {
        // todo: add snapshot behavior

        loadHistoricalEventsForAggregates([aggregate], eventService.findAllEventsForAggregate(aggregate))

    }

    void loadCurrentState(List<Aggregate> aggregates) {
        loadHistoricalEventsForAggregates(aggregates, eventService.findAllEventsForAggregates(aggregates))
    }

    void loadHistoryUpTo(Aggregate aggregate, int targetRevision) {
        aggregate.loadFromPastEvents(eventService.findAllEventsForAggregateSinceRevision(aggregate, targetRevision))
    }

    void loadHistoryUpTo(Aggregate aggregate, Date targetDate) {
        //aggregate.loadFromPastEvents(eventService.findAllEvents(aggregate, targetRevision))
    }

    void loadHistoryInRange(Aggregate aggregate, Date begin, Date end) {
        loadHistoricalEventsForAggregates([aggregate], eventService.findAllEventsForAggregateInRange(aggregate, begin, end))
    }

    void loadHistoryInRange(List<Aggregate> aggregates, Date begin, Date end) {
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

    private void loadHistoricalEventsForAggregates(List<Aggregate> aggregates, List<Event> events) {

        JsonSlurper slurper = new JsonSlurper()
        Map<UUID, Aggregate> aggregateLookup = aggregates.collectEntries {Aggregate it ->[(it.id): it]}
        println "Building up $aggregates from $events"
        rx.Observable.from(events)
            .map({Event event->
                event.restoreData(slurper.parseText(event.data) as Map)
                event
            })
            .groupBy({((Event)it).aggregateId})
            .flatMap({
                it.reduce([], {l, item-> l += item})
                .map({List collectedEvents-> return ((Aggregate)aggregateLookup[it.key]).loadFromPastEvents(collectedEvents)})
            })
            .subscribe({it}, {log.error("Unable to load events: ", it)}, {})
    }
}
