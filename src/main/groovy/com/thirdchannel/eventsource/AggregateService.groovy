package com.thirdchannel.eventsource

/**
 * Loads and persists {@link Aggregate}s.
 *
 * @author steve
 */
interface AggregateService {

    void setEventService(EventService eventService)
    EventService getEventService()

    Aggregate get(UUID id)
    List<Aggregate> getAll(List<UUID> ids)
    Aggregate getOrCreate(UUID id, String aggregateDescription)

    boolean exists(UUID aggregateId)

    /**
     * Grabs the current revision of the aggregate, ideally checked before saving events
     * to ensure that items are not placed out of order
     */
    int getCurrentRevision(UUID id)

    /**
     * Updates an Aggregate with an expected revision
     */
    boolean update(Aggregate aggregate, int expectedRevision)

    /**
     * Saves a new aggregate.
     */
    boolean save(Aggregate aggregate)

    /**
     * Persist the aggregate & its events within a transaction.
     * Use the expectedRevision to ensure that we can only update records that have no not been modified since, then
     * save the events. Rollback the transaction (or abort) if the expectedRevision doesn't match
     */
    boolean save(Aggregate aggregate, int expectedRevision, List<Event> events)
}
