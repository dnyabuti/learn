Create index via kibana

```
curl -XPUT "http://localhost:9200/my_movies" -H 'Content-Type: application/json' -d'
{
  "mappings": {
      "properties": {
        "name": {
          "type":"text"
        },
        "actor_count": {
          "type": "integer"
        },
        "date": {
          "type":"date"
        }
      }
  }
}'
```

Crate index and specify number of shards and replicas

```
curl -XPUT "http://localhost:9200/test_my_movies" -H 'Content-Type: application/json' -d'
{
  "mappings": {
      "properties": {
        "name": {
          "type":"text"
        },
        "actor_count": {
          "type": "integer"
        },
        "date": {
          "type":"date"
        }
      }
  },
  "settings": {
      "number_of_shards": 2,
      "number_of_replicas": 3
  }
}'
```
Insert document with an ID

```
curl -XPUT "http://localhost:9200/my_movies/_doc/1" -H 'Content-Type: application/json' -d'
{
  "name": "Star Wars",
  "actor_count": "2000",
  "date": "2013-03-10"  
}'
```

Insert document without specifying ID

```
curl -XPOST "http://localhost:9200/my_movies/_doc" -H 'Content-Type: application/json' -d'
{
  "name": "Star Wars",
  "actor_count": "2000",
  "date": "2013-03-10"  
}'
```

Get Document

```
GET /my_movies/_doc/1
```

Delete Document 
```
DELETE /my_movies/_doc/1
```

Check cluster health

`curl -XGET http://localhost:9200/_cluster/health?pretty`

Use bulk api to insert more than one document at once

```
curl -XPOST "http://localhost:9200/_bulk" -H 'Content-Type: application/json' -d'
{"index":{"_index":"my_movies","_id":"2"}}
{"name":"A Beautiful Mind","actor_count":3,"date":"2003-09-10"}
{"index":{"_index":"my_movies","_id":"3"}}
{"name":"Avengers","actor_count":1000,"date":"2019-03-10"}
'
```