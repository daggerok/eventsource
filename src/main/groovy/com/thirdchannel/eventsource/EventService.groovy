package com.thirdchannel.eventsource

/**
 * DAO Service for loading and persisting {@link Event}s.
 *
 * @author steve pember
 */
interface EventService {

    // get
    // load all from Aggregate

    // load all from Aggregate in Date Range
    // load all since revision

    // save all events

    List<Event> findAllEventsForAggregate(Aggregate aggregate)
    List<Event> findAllEventsForAggregateSinceRevision(Aggregate aggregate, Integer integer)
    List<Event> findAllEventsForAggregateSinceDate(Aggregate aggregate, Date date)
    List<Event> findAllEventsForAggregateInRange(Aggregate aggregate, Date begin, Date end)

    void loadEventsForAggregates(List<Aggregate> aggregates)
    void loadEventsForAggregates(List<Aggregate> aggregates, Date begin, Date end)

    boolean save(Aggregate aggregate, List<Event> events)
    boolean save(Aggregate aggregate, Event event)
}
