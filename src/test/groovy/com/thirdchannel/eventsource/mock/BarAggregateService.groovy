package com.thirdchannel.eventsource.mock

import com.thirdchannel.eventsource.Aggregate
import com.thirdchannel.eventsource.AggregateService
import com.thirdchannel.eventsource.aggregates.Bar
import groovy.util.logging.Slf4j

/**
 * Rather than try to mock out jdbc or use something like mockito, hear I create an in-memory aggregateService which simply stores
 * the aggregates in a Map, keyed off of uuid.
 *
 * @author Steve Pember
 */
@Slf4j
class BarAggregateService implements AggregateService<Bar> {

    Map<UUID, ? extends Aggregate> database

    BarAggregateService() {
        database = [:]
    }


    @Override
    Bar get(UUID id) {
        if (database.containsKey(id)) {
            new Bar(id: id)
        } else {
        null
        }
    }

    @Override
    List<Bar> getAll(List<UUID> ids) {
        List<Bar> bars = []
        ids.each {UUID id ->
            Bar bar = get(id)
            if (bar) {
                bars.add(bar)
            }
        }

    }

    @Override
    Bar getOrCreate(UUID id) {
        get(id)?:new Bar()
    }

    @Override
    boolean exists(UUID id) {
        database.containsKey(id)
    }

    @Override
    int getCurrentRevision(UUID id) {
        (database[id] as Bar).revision
    }

    @Override
    int update(Bar aggregate, int expectedRevision) {
        if (database[aggregate.id].revision == expectedRevision) {
            database[aggregate.id] = aggregate
            1
        } else {
            0
        }
    }

    @Override
    int save(Bar aggregate) {
        save([aggregate])
    }

    @Override
    int save(List<Bar> aggregates) {
        aggregates.each { Bar bar->
            database[bar.id] = bar
        }
        aggregates.size()
    }
}
