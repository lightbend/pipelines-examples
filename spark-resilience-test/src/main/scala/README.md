# Spark Resilience Test

This example attempts to validate that the spark components in a Pipeline are resilient to failure.
It creates 3 components:
- A data producer ingress,
- A processor, and
- A stateful validator egress.

The producer creates a monotonically increasing index with a timestamp

The egress keeps track of the indexes received and detects if any gaps in the stream occur

The processor is an ephemeral pass-through. Like a suicidal monkey, it kills itself randomly.
If all resilience features are working properly, it must come alive and keep its work where it left, leaving no holes in the data stream. 

Note: We have currently determined that spark-driver pods are not correctly reporting health. 
While this gets solved, the suicidal monkey will stay alive. 


The egress should detect and report if this is not the case.

