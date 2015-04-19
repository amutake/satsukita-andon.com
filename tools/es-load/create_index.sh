#!/bin/bash

# curl -XDELETE 'http://localhost:9200/andon'

curl -XPUT 'http://localhost:9200/andon/' -d '{
  "settings": {
    "analysis": {
      "filter": {
        "pos_filter": {
          "type": "kuromoji_part_of_speech",
          "stoptags": ["助詞-格助詞-一般", "助詞-終助詞"]
        },
        "greek_lowercase_filter": {
          "type": "lowercase",
          "language": "greek"
        }
      },
      "analyzer": {
        "my_analyzer": {
          "type": "custom",
          "tokenizer": "kuromoji_tokenizer",
          "filter": [
            "pos_filter",
            "greek_lowercase_filter",
            "kuromoji_baseform",
            "kuromoji_stemmer",
            "cjk_width",
            "stop",
            "snowball"
          ]
        }
      }
    }
  },
  "mappings": {
    "articles": {
      "_source": {
        "enabled": true
      },
      "_all": {
        "enabled": true,
        "analyzer": "my_analyzer"
      },
      "properties": {
        "title": {
          "type": "string",
          "index": "analyzed",
          "analyzer": "my_analyzer"
        },
        "text": {
          "type": "string",
          "index": "analyzed",
          "analyzer": "my_analyzer"
        },
        "genre": {
          "type": "string",
          "index": "analyzed",
          "analyzer": "my_analyzer"
        }
      }
    }
  }
}'
