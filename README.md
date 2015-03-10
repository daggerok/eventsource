# Event Source Library

This is a small library for working with Event Sourced aggregates and events. It provides functionality around process, 
querying, etc., but leaves it up to implementations to deal with the persistance specifics (e.g. using Postgres versus Redis,
SpringTemplate versus JOOQ).

## Working with Aggregates and Events




## TODO:
* add feature to block loading of historical events more than once. Perhaps a transient 'trackingRevision' or resetting revision to 0 in memory when the aggregate is loaded?
* cleanup apis on service to force more through EventSourceService