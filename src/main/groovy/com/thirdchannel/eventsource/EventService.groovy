package com.thirdchannel.eventsource

/**
 * Loads and persists {@link Event}s.
 *
 * @author steve pember
 */
interface EventService {

    /**
     *
     * @param aggregate
     * @return
     */
    List<Event> findAllEventsForAggregate(Aggregate aggregate)

    /**
     *
     * @param aggregate
     * @return
     */
    List<Event> findAllEventsForAggregates(List<? extends Aggregate> aggregates)

    /**
     *
     * @param aggregate
     * @param begin
     * @param end
     * @return
     */
    List<Event> findAllEventsForAggregateInRange(Aggregate aggregate, Date begin, Date end)

    /**
     *
     * @param aggregate
     * @param begin
     * @param end
     * @return
     */
    List<Event> findAllEventsForAggregatesInRange(List<? extends Aggregate> aggregate, Date begin, Date end)

    /**
     *
     * @param aggregate
     * @param revision
     * @return
     */
    List<Event> findAllEventsForAggregateUpToRevision(Aggregate aggregate, int revision)

    /**
     *
     * @param aggregate
     * @param date
     * @return
     */
    List<Event> findAllEventsForAggregateUpToDateEffective(Aggregate aggregate, Date date)

    /**
     *
     * @param event
     * @return
     */
    boolean save(Event event)

    /**
     *
     * @param events
     * @return
     */
    boolean save(List<? extends Event> events)

}
