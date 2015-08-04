# Event Source Library

This is a small library for working with Event Sourced aggregates and events. It provides functionality around process, 
querying, etc., but leaves it up to implementations to deal with the persistence specifics (e.g. using Postgres versus Redis,
SpringTemplate versus JOOQ).

## What is Event Sourcing?

TODO, but see ()


## Key Terms: Aggregates, Events, Snapshots


### Aggregate
An Aggregate represents the 'container' or root for a stream of Events. Aside from the properties defined within the interface, all implementation
properties should be transient (e.g. not saved directly to the database along with the aggregate). At least, in the 'pure' form of ES.
It's fully possible when using this library to create several services around each of your intended Aggregates, which know how to persist
specific attributes to the database. One such approach is to use multi-table inheritance to subclass several Aggregates off of some base Aggregate,
each

### Event
An Event represents a single atomic unit of state change for an Aggregate. It represents something successful that occurred within our system
against an Aggregate.

### Snapshot
A Snapshot is exactly what it sounds like, a moment-in-time recording of the state of an Aggregate. These Snapshots are used as a starting point for rebuilding
the state of an Aggregate without having to process a potentially unwieldy number of events.

## Working with this Library

The basic design structure is as follows:

* Each Aggregate in your system operated on by a dedicated 'EventSourceService'. This is fine if you have only one Aggregate in your system (Microservices, anyone?), but can become cumbersome in a system with multiple Aggregates.
* Each EventSourceService is backed by a dedicated AggregateService and EventService. These are essentially DAO classes that know only how to CR (No Update or Delete!... well, the AggregateService does update the revision).
* The EventSourceService utilizes the Aggregate and Event Services and performs the basic ES operations: processing new events, loading an Aggregate to any point in time (most often will be 'Current'), handling Event Serialization and De-Serialization.

_Note:_ I'm not super pleased with the current design and naming; it's subject to change. The goal was to 'hide' the AggregateService and EventService behind a parent Object / API layer, but in practice users tend to want to build queries into the AggregateService directly and access that directly, circumventing the ESService.

## Current Serialization Mechanism
An Event Class should mark any data that should be persisted as part of the event with the @EventData annotation. The default mechanism will take those properties and serialize them to JSON using Groovy's JsonBuilder. When pulling events from the database, it will use Groovy's JsonSlurper. This logic is contained within JsonBuilderEventSerializer.

This behavior can be overwritten by implementing your own EventSerializer and calling setEventSerializer on an instance of EventSourceService.



## TODO:
* Implement Snapshot
* cleanup apis on service to force more through EventSourceService