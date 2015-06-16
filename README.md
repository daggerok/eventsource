# Event Source Library

This is a small library for working with Event Sourced aggregates and events. It provides functionality around process, 
querying, etc., but leaves it up to implementations to deal with the persistance specifics (e.g. using Postgres versus Redis,
SpringTemplate versus JOOQ).

## Working with Aggregates and Events


### Aggregate
An Aggregate represents the 'container' or root for a stream of Events. Aside from the properties defined within the interface, all implementation
properties should be transient (e.g. not saved directly to the database along with the aggregate).

### Event
An Event represents a single atomic unit of state change for an Aggregate. It represents something successful that occurred within our system
against an Aggregate.





## TODO:
* Implement Snapshot
* Add ability to set JSON serializer / deserializer (currently uses Groovy's JSON slurper and builder)
* cleanup apis on service to force more through EventSourceService