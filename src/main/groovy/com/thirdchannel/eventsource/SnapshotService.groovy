package com.thirdchannel.eventsource

/**
 * DAO Service for loading and persisting {@link Snapshot}s.
 *
 * @author steve pember
 */
interface SnapshotService {
    int getInterval()
    void setInterval()
}
