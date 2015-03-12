package com.thirdchannel.eventsource

/**
 * Loads and persists {@link Aggregate}s.
 *
 * @author steve
 */
interface AggregateService {

    /**
     *
     * @param id
     * @return
     */
    Aggregate get(UUID id)

    /**
     *
     * @param ids
     * @return
     */
    List<Aggregate> getAll(List<UUID> ids)

    /**
     *
     * @param id
     * @param description
     * @return
     */
    Aggregate getOrCreate(UUID id, String description)

    /**
     * Create a new Aggregate with the attached description
     *
     * @param description
     * @return
     */
    Aggregate create(String description)

    /**
     *
     * @param aggregateId
     * @return true if there is an aggregate with the provided UUID, false otherwise
     */
    boolean exists(UUID id)

    /**
     * Grabs the current revision of the aggregate, ideally checked before saving events
     * to ensure that items are not placed out of order
     */
    int getCurrentRevision(UUID id)

    /**
     * Updates an Aggregate with an expected revision. The underlying update method should 'where' by aggregate id and expectedRevision
     *
     * @param aggregate {@link Aggregate}
     * @param expectedRevision int The expected revision number we'll find for this aggregate in the database. This update should fail if there is no match for aggregateId and expectedRevision
     * @return the number of rows updated. Should really be 1. 0 implies that your aggregate is out of version, and anything larger than 1 is a disaster
     * @see Aggregate
     */
    int update(Aggregate aggregate, int expectedRevision)

    /**
     * Saves a new aggregate.
     * @param aggregate The {@link Aggregate} to save
     * @return the number of aggregates saved. Should be 1 or 0
     */
    int save(Aggregate aggregate)
}
