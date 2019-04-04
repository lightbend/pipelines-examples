pipectl streamlets list
 
pipectl blueprint use CallRecordStreamingIngress CDRIngress
pipectl blueprint use CallStatsAggregator Aggregator
pipectl blueprint connect CDRIngress.accepted Aggregator.in
pipectl blueprint use CallAggregatorConsoleEgress ConsoleEgress
pipectl blueprint connect Aggregator.out ConsoleEgress.in
