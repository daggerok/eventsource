package com.thirdchannel.eventsource

/**
 * Loads and persists {@link Snapshot}s.
 *
 * @author steve pember
 */
interface SnapshotService {
    int getInterval()
    void setInterval()
}
