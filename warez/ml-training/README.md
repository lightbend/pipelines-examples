# Recommender model generation

The iPython notebook does the following:

1. Reads data from the `data/` folder. The `data` folder contains 2 variant of data files - 1 containing a large dataset and the other a smaller one.
2. Builds a neural network model for learning
3. Runs the training
4. Exports the model in TensorFlow format
5. Generates an Avro binary file containing all information needed to be transferred to the model serving streamlet

**Note:** In the current implementation of the notebook, we have the `model_path` hardcoded. This is the folder where all models, graphs, avro files are generated. Needs to change appropriately when running the notebook.


## Mapping Ids

In `warez` the product (sku) ids and customer ids are modeled as strings (UUIDs) while the machine learning classifier neural network needs integers. Hence we do a mapping of the UUIDs to a unique integer value for all the customer and product ids.

This mapping information also needs to be exported along with the model itself. The notebook also does this.

## Model Id

In the current implementation, the model id for the generated model is specified as "recommender-model-[current timestamp]". This id will be present in the final avro that the notebook generates.

## Model Avro Schema

The avro file that the notebook generates is based on the schema present in `avro/` folder, named `RecommenderModel.avsc`. This schema has to match with the one present on the Scala side where streamlets are defined. The schema is:

```
{
  "namespace": "warez",

  "type": "record",
  "name": "RecommenderModel",

  "fields": [
    {
      "name": "modelId",
      "type": "string"
    },
    {
      "name": "tensorFlowModel",
      "type": "bytes"
    },
    {
      "name": "productMap",
      "type": {
        "type": "map",
        "values": "int"
      }
    },
    {
      "name": "customerMap",
      "type": {
        "type": "map",
        "values": "int"
      }
    }
  ]
}
```

## Generated Avro

The notebook generates 2 Avro files:

* With schema embedded within the binary file saved in `recommender.avro` under the `model_path` folder. This can be imported to the streamlet for model serving
* Without schema embedded within the binary file saved in `recommender-no-schema.avro` under the `model_path` folder. This can also be imported to the streamlet for model serving
