# Warez Pipelines Sample Application

# Streamlets

## Product Search Egress

The Product Search egress performs two functions:

1. Persist products joined with updated stock and price information into an ElasticSearch index called `products`
2. Expose an Http endpoint to search the `products` index using some kind of criteria.

This egress requires the user to setup the ElasticSearch infrastructure.

To query the endpoint you must lookup the egress endpoint using the CLI: `kubectl pipelines status warez`.

Then issue a GET request to the endpoint with search criteria.

Ex)
 
```
$ curl http://warez.apps.purplehat.lightbend.com/products-search/search/black hole?count=5 | jq '.'
[
  {
    "description": "A cartoon hole that can be applied to any surface.  https://www.youtube.com/watch?v=znzkdE-QQp0",
    "id": "123456789",
    "keywords": [
      "black",
      "hole",
      "gag",
      "plot device",
      "roger rabbit"
    ],
    "name": "Acme Portable Hole",
    "skus": [
      {
        "id": "1",
        "name": "Small Hole",
        "price": 5,
        "stock": 10
      },
      {
        "id": "2",
        "name": "Medium Hole",
        "price": 10,
        "stock": 10
      },
      {
        "id": "3",
        "name": "Large Hole",
        "price": 20,
        "stock": 15
      }
    ]
  }
]
```

# Setup

## ElasticSearch

ElasticSearch can be installed from the [`stable` Helm Chart Repository](https://github.com/helm/charts/tree/master/stable/elasticsearch).

Login to OpenShift as a user with the `cluster-admin` role.

Create an `elasticsearch` namespace and switch to it.

```
oc create namespace elasticsearch
oc project elasticsearch
```

ElasticSearch Pod containers must run under a `ServiceAccount` that is granted more privileged 
`SecurityContextContraints` (SCC) then the default `restricted` SCC because they run in Docker privileged mode and as 
the root user.  Assign the `ServiceAccounts` that the ElasticSearch Helm Chart will create to the `privileged` and 
`anyuid` OpenShift `SecurityContextConstraints` (you can do this before the `ServiceAccounts` exist).

```
oc adm policy add-scc-to-user privileged -z pipelines-elasticsearch-client
oc adm policy add-scc-to-user privileged -z pipelines-elasticsearch-data
oc adm policy add-scc-to-user privileged -z pipelines-elasticsearch-master
oc adm policy add-scc-to-user anyuid -z pipelines-elasticsearch-client
oc adm policy add-scc-to-user anyuid -z pipelines-elasticsearch-data
oc adm policy add-scc-to-user anyuid -z pipelines-elasticsearch-master
```

Install the Helm Chart into the `elasticsearch` namespace.

```
# Use the appropriate namespace for Tiller
export TILLER_NAMESPACE=tiller
# Install the chart into the elasticsearch namespace
helm install --name pipelines-elasticsearch stable/elasticsearch --namespace elasticsearch
```

Wait for all pods to be ready.

```
$ oc get pod
NAME                                             READY     STATUS    RESTARTS   AGE
pipelines-elasticsearch-client-887956fdb-br2hd   1/1       Running   0          8m
pipelines-elasticsearch-client-887956fdb-gj7bb   1/1       Running   0          8m
pipelines-elasticsearch-data-0                   1/1       Running   0          8m
pipelines-elasticsearch-data-1                   1/1       Running   0          6m
pipelines-elasticsearch-master-0                 1/1       Running   0          8m
pipelines-elasticsearch-master-1                 1/1       Running   0          7m
pipelines-elasticsearch-master-2                 1/1       Running   0          6m
```

Test client connectivity.  Setup a port-forward to the service.

```
oc port-forward service/pipelines-elasticsearch-client 9200:9200
```

`curl` the default route in another shell.

```
$ curl http://127.0.0.1:9200
{
  "name" : "pipelines-elasticsearch-client-887956fdb-br2hd",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "DqBEzwcCRueHwWL-VeAxQg",
  "version" : {
    "number" : "6.5.4",
    "build_flavor" : "oss",
    "build_type" : "tar",
    "build_hash" : "d2ef93d",
    "build_date" : "2018-12-17T21:17:40.758843Z",
    "build_snapshot" : false,
    "lucene_version" : "7.5.0",
    "minimum_wire_compatibility_version" : "5.6.0",
    "minimum_index_compatibility_version" : "5.0.0"
  },
  "tagline" : "You Know, for Search"
}
```

Create an index for products.

```
curl -X PUT "localhost:9200/products?pretty"
curl -X GET "localhost:9200/_cat/indices?v"
```

Deploy the application and push a sample `Product` to the ingress.  i.e. 

```
curl -i -X POST warez.apps.purplehat.lightbend.com/products -H "Content-Type: application/json" --data '@test-data/product-black-hole.json'
```

Confirm the `Product` was written to the index.

```
curl -X GET "localhost:9200/products/_doc/123456789?pretty"
```