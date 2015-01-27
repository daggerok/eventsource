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

//    Aggregate findOrCreateById(UUID aggregateId) {
//
//    }
    /**
     * Saves an aggregate and it's uncomitted events. Applies revision updates to the
     *
     * @param aggregate
     * @param events
     */
    boolean save(Aggregate aggregate) {
        // save Uncomitted events. For each uncomitted event,increment the revision on the aggregate and set the
        // revision on the event.
        // pass to the aggregateService for persisting, with the idea that it will save both the Aggregate and events
        // within a Transaction, if possible
        Integer oldRevision = aggregate.revision
        for (Event event: aggregate.getUncommittedEvents()) {
            //increment the revision for both the aggregate and its events
            aggregate.revision++
            event.revision = aggregate.revision
            //todo: add a revision check here, or rely on the implementations to do that?

        }
        if (aggregateService.save(aggregate, oldRevision, aggregate.getUncommittedEvents())) {
            // finally, mark the aggregate's changes as committed to 'flush' the events and prepare for more
            log.debug("Uncomitted Events persisted. Clearly events from aggregate")
            aggregate.markEventsAsCommitted()
            true
        } else {
            log.error("AggregateService failed to persist aggregates and events")
            false
        }

    }

    void loadCurrentState(Aggregate aggregate) {
        // todo: add snapshot behavior
        aggregate.loadFromPastEvents(eventService.findAllEventsForAggregate(aggregate))
    }

    void loadHistoryUpTo(Aggregate aggregate, Integer targetRevision) {
        aggregate.loadFromPastEvents(eventService.findAllEventsForAggregateSinceRevision(aggregate, targetRevision))
    }

    void loadHistoryUpTo(Aggregate aggregate, Date targetDate) {
        //aggregate.loadFromPastEvents(eventService.findAllEvents(aggregate, targetRevision))
    }

    void loadHistoryInRange(Aggregate aggregate, Date begin, Date end) {
        aggregate.loadFromPastEvents(eventService.findAllEventsForAggregateInRange(aggregate, begin, end))
    }



    // current aggregate

    // process events -> check for revision, check for order of events


    // events are applied directly onto the aggregate.
    // The event store should be used to persist the uncomitted events on an aggregate, then increments the revision on
    // the aggregate

    // should be a method for loading events on an aggregate between a date range, on a specific date, all before / all after a date,
    // or all events. The method calls should use snapshot service



}
