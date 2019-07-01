# Pipelines Examples Applications

## `sensor-data-scala`

A simple pipeline that processes events from a wind turbine farm.

## `sensor-data-java`

The same as `sensor-data-scala`, but implemented using the Java DSL.

## `call-record-aggregator`

An aggregation of user call record data (metadata of phone calls).

## `spark-sensors`

A simple pipeline that generates events from energy devices.

## `spark-resilience-test`

A simple pipeline that generates events from energy devices.  This pipeline
will fail based on a pre-defined probability percentage.  Its purpose is to
demonstrate the failure recovery features of Pipelines and Spark.

## `warez`

An event-based e-commerce streaming platform.  Events are generated based on
user events such as purchases and merchant actions such as the addition of
products and their stock numbers.

# Pipelines Feature Grid

| Application         | Akka Streams (Scala) | Akka Streams (Java) | Spark | Testkit | Ingress | Egress                  | Auto Data Generation    |
|---------------------|----------------------|---------------------|-------|---------|---------|-------------------------|-------------------------|
| `sensor-data-scala` | Yes                  | No                  | Yes   | No      | HTTP    | stdout (logs)           | Yes (Client Lua Script) |
| `sensor-data-java`  | No                   | Yes                 | No    | No      | HTTP    | stdout (logs)           | Yes (Client Lua Script) |
| `call-record-aggregator` | Yes             | Yes                 | Yes   | Yes     | HTTP    | stdout (logs)           | Yes                     |
| `spark-sensors`     | Yes                  | No                  | No    | No      | HTTP    | stdout (logs)           | Yes                     |
| `spark-resilience-test` | Yes              | No                  | No    | Yes     | HTTP    | stdout (logs)           | Yes                     |
| `warez`             | Yes                  | No                  | Yes   | Yes     | HTTP    | ElasticSearch, HTTP API | No                      |

# Running Examples

Consult the [Pipelines Documentation](https://developer.lightbend.com/docs/pipelines/current/)
for instructions on building, deploying and running Pipelines applications.

---

**NOTE**

Before building any examples remember to update the Docker registry in the `target-env.sbt` file.

---
