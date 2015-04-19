# Elasticsearch Bulk Load Tool for satsukita-andon.com

## Requirements

- elasticsearch-1.5
- [kuromoji-plugin-2.5](https://github.com/elastic/elasticsearch-analysis-kuromoji)

## Usage

```sh
$ES_HOME/bin/plugin install elasticsearch/elasticsearch-analysis-kuromoji/2.5.0
$ES_HOME/bin/elasticsearch # or restart elasticsearch
./create_index.sh
sbt assembly
java -jar ./target/scala-2.11/es-load.jar
```
