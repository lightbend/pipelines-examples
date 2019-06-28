# `sensor-data-scala`

A simple pipeline that processes events from a wind turbine farm.

# Required configuration

`valid-logger.log-level`

Log level for `*-logger` streamlets to log to.  Ex) `info`

`valid-logger.msg-prefix` - Log line prefix for `*-logger` streamlets to include.  Ex) `VALID`

kubectl-pipelines deploy docker-registry-default.purplehat.lightbend.com/lightbend/sensor-data-scala:382-55e76fe-dirty valid-logger.log-level=info valid-logger.msg-prefix=VALID


# Generate data

1. Deploy app with `kubectl pipelines deploy [image]`
2. Get `sensor-data` ingress HTTP endpoint with `kubectl pipelines status sensor-data-scala`
3. Pick a test data file from `./test-data`

Ex) Send `04-moderate-breeze.json` data to ingress relative to project root directory.

```
curl -i -X POST sensor-data-scala.apps.purplehat.lightbend.com/sensor-data -H "Content-Type: application/json" --data '@test-data/04-moderate-breeze.json'
```

## Using [`wrk`](https://github.com/wg/wrk) benchmarking tool.

To send a continuous stream of data.

### Install

* Ubuntu: `apt-get install wrk`
* MacOS: `brew install wrk`

### Run

Ex)

```
wrk -c 400 -t 400 -d 500 -s wrk-04-moderate-breeze.lua http://sensor-data-scala.apps.purplehat.lightbend.com/sensor-data
```
