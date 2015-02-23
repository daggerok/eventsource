package com.thirdchannel.eventsource

import groovy.util.logging.Slf4j

/**
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

    Aggregate getOrCreate(UUID aggregateId, String description) {
        aggregateService.getOrCreate(aggregateId, description)
    }

//    Aggregate findOrCreateById(UUID aggregateId) {
//
//    }
    /**
     * Saves an aggregate and its uncommitted events. Applies revision updates to the
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

        if (aggregateService.save(aggregate, oldRevision, aggregate.uncommittedEvents)) {
            // finally, mark the aggregate's changes as committed to 'flush' the events and prepare for more
            log.debug("Uncomitted Events persisted. Clearly events from aggregate")
            aggregate.markEventsAsCommitted()
            true
        } else {
            log.error("AggregateService failed to persist aggregates and events")
            false
        }
    }

    /*
        The majority of these methods are simply pass-throughs, with the purpose of keeping a common barrier behind
        EventSourceService
     */

    void loadCurrentState(Aggregate aggregate) {
        // todo: add snapshot behavior
        aggregate.loadFromPastEvents(eventService.findAllEventsForAggregate(aggregate))
    }

    void loadCurrentState(List<Aggregate> aggregates) {
        eventService.loadEventsForAggregates(aggregates)
    }

    void loadHistoryUpTo(Aggregate aggregate, int targetRevision) {
        aggregate.loadFromPastEvents(eventService.findAllEventsForAggregateSinceRevision(aggregate, targetRevision))
    }

    void loadHistoryUpTo(Aggregate aggregate, Date targetDate) {
        //aggregate.loadFromPastEvents(eventService.findAllEvents(aggregate, targetRevision))
    }

    void loadHistoryInRange(Aggregate aggregate, Date begin, Date end) {
        aggregate.loadFromPastEvents(eventService.findAllEventsForAggregateInRange(aggregate, begin, end))
    }

    void loadHistoryInRange(List<Aggregate> aggregates, Date begin, Date end) {
        eventService.loadEventsForAggregates(aggregates, begin, end)
    }

    // current aggregate

    // process events -> check for revision, check for order of events

    // events are applied directly onto the aggregate.
    // The event store should be used to persist the uncomitted events on an aggregate, then increments the revision on
    // the aggregate

    // should be a method for loading events on an aggregate between a date range, on a specific date, all before / all after a date,
    // or all events. The method calls should use snapshot service
}
