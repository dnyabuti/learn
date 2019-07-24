# Elasticsearch 7 and the Elastic Stack

## Section 1: Test installation

* Get shakespear mapping

`wget http://media.sundog-soft.com/es7/shakes-mapping.json`

* Add mapping to elastic search

`curl -H "Content-Type: application/json" -XPUT localhost:9200/shakespeare --data-binary @shakes-mapping.json`

* Get docs
`wget http://media.sundog-soft.com/es7/shakespeare_7.0.json`

* Add docs to elasticsearch

`curl -H "Content-Type: application/json" -XPOST '127.0.0.1:9200/shakespeare/_bulk' --data-binary @shakespeare_7.0.json`

* Search for to be or not to be

```{bash}
curl -H "Content-Type: application/json" -XGET '127.0.0.1:9200/shakespeare/_search?pretty' -d '
{
    "query":{
        "match_phrase": {
            "text_entry": "to be or not to be"
        }
    }
}'
```
## Section 2: Mapping & Indexing Data

### Get dataset

Get movielens [data](http://files.grouplens.org/datasets/movielens/ml-latest-small.zip) and unzip

Create curl helper script in bin directory

`mkdir bin`

Create a file in the bin directory named curl with the following content

> *curl*
```
#!/bin/bash
/usr/bin/curl -H "Content-Type: application/json" "$@"
```
Create Mapping

```
bin/curl -XPUT 127.0.0.1:9200/movies -d '
{
    "mappings": {
        "properties": {
            "year": {
                "type":"date"
            }
        }
    }
}'
```

View mapping

`bin/curl -XGET 127.0.0.1:9200/movies/_mapping`
```
{"movies":{"mappings":{"properties":{"year":{"type":"date"}}}}}
```

Add movie
```
bin/curl -XPOST 127.0.0.1:9200/movies/_doc/109487 -d '
{
    "genre": ["IMAX", "Sci-Fi"],
    "title": "Interstellar",
    "year": 2014
}'
```

Search all movies

`bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty`

### Bulk API

We will insert data in movies.json file using the bulk API. 

`wget http://media.sundog-soft.com/es7/movies.json`

Assumming the movies.json file is in the data directory, run the following command

`bin/curl -XPUT 127.0.0.1:9200/_bulk --data-binary @data/movies.json`

### Updating data

Update title

Full update... only fields contained in update are in the eventual doc. Use `PUT`

```
bin/curl -XPUT 127.0.0.1:9200/movies/_doc/109487?pretty -d '
{
    "genre": ["IMAX", "Sci-Fi"],
    "title": "Interstellar Foo",
    "year": 2014
}'
```
For partial update use `POST`. Only the fields included are updated

bin/curl -XPUT 127.0.0.1:9200/movies/_doc/109487?pretty -d '
{
    "title": "Interstellar"
}'

### Delete data

First find document ID and then use `DELETE verb to delete document.

1. Get document id for _Dark Night_

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?q=Dark
{"took":1,"timed_out":false,"_shards":{"total":1,"successful":1,"skipped":0,"failed":0},"hits":{"total":{"value":1,"relation":"eq"},"max_score":1.5442266,"hits":[{"_index":"movies","_type":"_doc","_id":"58559","_score":1.5442266,"_source":{ "id": "58559", "title" : "Dark Knight, The", "year":2008 , "genre":["Action", "Crime", "Drama", "IMAX"] }}]}}
```
2. Delete document with ID 58559

`bin/curl -XDELETE 127.0.0.1:9200/movies/_doc/58559?pretty`


### Dealing with concurrency

Updates should be restricted to a particular sequence number.

Suppose we want to update doc with id 109487

1. Retrieve the sequence number
```
bin/curl -XGET 127.0.0.1:9200/movies/_doc/109487?pretty
{
  "_index" : "movies",
  "_type" : "_doc",
  "_id" : "109487",
  "_version" : 2,
  "_seq_no" : 5,
  "_primary_term" : 1,
  "found" : true,
  "_source" : {
    "genre" : [
      "IMAX",
      "Sci-Fi"
    ],
    "title" : "Interstellar Foo",
    "year" : 2014
  }
}
```
Notice that the sequence numbe is 5.

2. Update the document by specifying the sequence number

```
bin/curl -XPUT "127.0.0.1:9200/movies/_doc/109487?if_seq_no=5&if_primary_term=1" -d '
{
    "genre": ["IMAX", "Sci-Fi"],
    "title": "Intersteller Foo",
    "year": 2014
}'
...
{"_index":"movies","_type":"_doc","_id":"109487","_version":3,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":7,"_primary_term":1}
```

Note that the new sequence number is 7. If we tried to run the same update an error should be thrown

```
bin/curl -XPUT "127.0.0.1:9200/movies/_doc/109487?if_seq_no=5&if_primary_term=1" -d '
{
    "genre": ["IMAX", "Sci-Fi"],
    "title": "Intersteller Foo Error",
    "year": 2014
}'
...
{"error":{"root_cause":[{"type":"version_conflict_engine_exception","reason":"[109487]: version conflict, required seqNo [5], primary term [1]. current document has seqNo [7] and primary term [1]","index_uuid":"2NZ0H2NmSQqKtrE-KLHWaw","shard":"0","index":"movies"}],"type":"version_conflict_engine_exception","reason":"[109487]: version conflict, required seqNo [5], primary term [1]. current document has seqNo [7] and primary term [1]","index_uuid":"2NZ0H2NmSQqKtrE-KLHWaw","shard":"0","index":"movies"},"status":409}
```

### Using Analyzers and Tokenizers

* **keyword** - exact match only, case sensitive
* **text** - allows analyzers 

Search for movies with Star Trek in title

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match": {
            "title": "Star Trek"
        }
    }
}'
```
Returns

```
{
  "took" : 3,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 2,
      "relation" : "eq"
    },
    "max_score" : 2.5194323,
    "hits" : [
      {
        "_index" : "movies",
        "_type" : "_doc",
        "_id" : "135569",
        "_score" : 2.5194323,
        "_source" : {
          "id" : "135569",
          "title" : "Star Trek Beyond",
          "year" : 2016,
          "genre" : [
            "Action",
            "Adventure",
            "Sci-Fi"
          ]
        }
      },
      {
        "_index" : "movies",
        "_type" : "_doc",
        "_id" : "122886",
        "_score" : 0.66992384,
        "_source" : {
          "id" : "122886",
          "title" : "Star Wars: Episode VII - The Force Awakens",
          "year" : 2015,
          "genre" : [
            "Action",
            "Adventure",
            "Fantasy",
            "Sci-Fi",
            "IMAX"
          ]
        }
      }
    ]
  }
}
```

Includes partial match for Star Wars but with a lower score.

Try the same for genre but using `match_phrase`

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match_phrase": {
            "grenre": "sci"
        }
    }
}'
```

Picks up **Sci-Fi** as well because genre is analyzed. Genre is being treated as text but we want this field to be specific. In order to fix this the index needs to be deleted and created again

`curl -XDELETE 127.0.0.1:9200/movies`

Create new index

```
bin/curl -XPUT 127.0.0.1:9200/movies -d '
{
    "mappings": {
        "properties": {
            "id": {
                "type":"integer"
            },
            "year":{
                "type":"date"
            },
            "genre":{
                "type":"keyword"
            },
            "title":{
                "type":"text",
                "analyzer": "english"
            }
        }
    }
}'
```

Insert data

```
bin/curl -XPUT 127.0.0.1:9200/_bulk?pretty --data-binary @data/movies.json
```

Now running 
```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match_phrase": {
            "grenre": "sci"
        }
    }
}'
```

Returns no hits. However, running

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match_phrase": {
            "grenre": "Sci-Fi"
        }
    }
}'
```

returns the expected results. It is case sensitive!

### Data Modeling, Parent/Child Relationships

Suppose we want to model the Star Wars series. The goal is to associate all the Star Wars movies with the Star Wars franchise

Create an index called series that will contain the Franchise and Movies we want to relate together with the franchise

> *Parent is `franchise` and `film` is the child
```
bin/curl -XPUT 127.0.0.1:9200/series -d '{
    "mappings": {
        "properties": {
            "film_to_franchise": {
                "type": "join",
                "relations": {
                    "franchise": "film"
                }
            }
        }
    }
}'
```

Get the data

`wget http://media.sundog-soft.com/es7/series.json`

Contains bulk insert statements

```
{ "create" : { "_index" : "series", "_id" : "1", "routing" : 1} }
{ "id": "1", "film_to_franchise": {"name": "franchise"}, "title" : "Star Wars" }
{ "create" : { "_index" : "series", "_id" : "260", "routing" : 1} }
{ "id": "260", "film_to_franchise": {"name": "film", "parent": "1"}, "title" : "Star Wars: Episode IV - A New Hope", "year":"1977" , "genre":["Action", "Adventure", "Sci-Fi"] }
{ "create" : { "_index" : "series", "_id" : "1196", "routing" : 1} }
{ "id": "1196", "film_to_franchise": {"name": "film", "parent": "1"}, "title" : "Star Wars: Episode V - The Empire Strikes Back", "year":"1980" , "genre":["Action", "Adventure", "Sci-Fi"] }
...
```

The first entry creates a franchise with **id** 1. The second entry creates a film with the title *Star Wars: Episode IV - A New Hope* and sets parent to 1

Insert the data

`bin/curl -XPUT 127.0.0.1:9200/_bulk?pretty --data-binary @data/series.json`

Get all films associated with the Star Wars franchise

```
bin/curl -XGET 127.0.0.1:9200/series/_search?pretty -d '
{
    "query": {
        "has_parent": {
            "parent_type": "franchise",
            "query": {
                "match": {
                    "title": "Star Wars"
                }
            }
        }
    }
}'
```

Find a franchise associated with a film

```
bin/curl -XGET 127.0.0.1:9200/series/_search?pretty -d '
{
    "query": {
        "has_child": {
            "type": "film",
            "query": {
                "match": {
                    "title": "The Force Awakens"
                }
            }
        }
    }
}'
```
## Section 3: Searching With Elasticsearch

### "Query Lite" Interface (URI Search)

Allows querying without providing a JSON body in the request. Drawback - requires URL encoding which makes it less readable

```
bin/curl -XGET "127.0.0.1:9200/movies/_search?q=title:star&pretty"
bin/curl -XGET "127.0.0.1:9200/movies/_search?q=+year>2010+title:trek&pretty"
```

### JSON Search

**filters** yes/no questions of the data. They are cacheable
+ `terms` match any exact values in a list match `{"terms": {"genre": ["Sci-Fi", "Adventure"]}}`
+ `range` Find numbers or dates within a given range (gt, gte, lt, lte) ```"range": {
                    "year":{
                        "gte":2010
                    }
                }```
+ `exists` Finds a doducment where a field exists `{"exists": {"field": "title"}}
+ `missing` Finds a doducment where a field is missing `{"missing": {"field": "title"}}`
+ `bool` Combines filters with boolean logic (must, must_not, should(equivalent to OR)). 

**queries** return data in terms of relavance
+ `match_all` Returns everything `{"match_all": {}}`
+ `match` Relevance search on a single field `{"match": {"year": 2010}}`
+ `multi_match` Run query on multiple fields ```{"multi_match": {"query": "star", "fields" ["title", "synopsis"]}}```
+ `bool` Like bool filter but results are scored by relevance. i.e results are not filtered out

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "bool": {
            "must":{
                "term": {
                    "title": "trek"
                }
            },
            "filter": {
                "range": {
                    "year":{
                        "gte":2010
                    }
                }
            }
        }
    }
}'
```

### Phrase Matching

Searches for terms in a document that occur in a specified order

*slop* - how far you are willing to let a term move to satisfy a phrase. e.g

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match_phrase": {
            "title": {
                "query": "star beyond", "slop": 1
            }
        }
    }
}'
```

The query above will match either **star beyond** or any combination of the two with a different word in the middle. e.g **Star Trek Beyond**. It will also match **beyond star**. 

> Returns documents with either star or wars but higher score for a document with both (**using `match`**)
```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match": {
            "title": "star wars"
        }
    }
}'
```

> Returns documents with star wars in title
```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match_phrase": {
            "title": "star wars"
        }
    }
}'
```

**Proximity query: ** If you increase the `slop` value to maybe 100, the query will return any document with the terms in the query but documents with the terms closer togeter have a higher rank.

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match_phrase": {
            "title": {
                "query": "star beyond", "slop": 100
            }
        }
    }
}'
```

> Get star wars movies created after 1980
```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "bool": {
            "must": {
                "match_phrase": {
                    "title": "Star Wars"
                }
            },
            "filter": {
                "range": {
                    "year": {
                        "gte": 1980
                    }
                }
            }
        }
    }
}'
```

### Pagination

Specify from and size

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?size=2&from=2&pretty

# OR

bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "from": 2,
    "size": 2,
    "query": {
        "match": {
            "genre": "Sci-Fi"
        }
    }
}'
```
> *from and size must occur before query*

### Sorting

Can be done by providing setting the sort parameter.

> Numerical fields

`bin/curl -XGET '127.0.0.1:9200/movies/_search?sort=year&pretty'`

A `string` firld that is analyzed for full-text search can't be used to sort documents because it exists in the inverted index as individual terms and not as the entire string. To fix this a `keyword` copy of the field can be stored.


> Delete index
```
bin/curl -XDELETE  127.0.0.1:9200/movies
```

> Create index with a copy of a title in the raw field
```
bin/curl -XPUT 127.0.0.1:9200/movies/ -d '
{
    "mappings": {
        "properties": {
            "title": {
                "type": "text",
                "fields": {
                    "raw": {
                        "type": "keyword"
                    }
                }
            }
        }
    }
}'
```
> import data
`bin/curl -XPUT 127.0.0.1:9200/_bulk --data-binary @data/movies.json`

And sorting can be done on the title field as such

`bin/curl -XGET '127.0.0.1:9200/movies/_search?sort=title.raw&pretty'`

### Filters (adv)

Find science fiction movies that do not have the term treck in the title that were released in the year 2010 through 2015 sorted by title
```
bin/curl -XGET '127.0.0.1:9200/movies/_search?sort=title.raw&pretty' -d '
{
    "query": {
        "bool": {
            "must": {
                "match": {
                    "genre": "Sci-Fi"
                }
            },
            "must_not": {
                "match": {
                    "title": "trek"
                }
            },
            "filter": {
                "range": {
                    "year": {
                        "gte": 2010, "lt": 2015
                    }
                }
            }
        }
    }
}'
```

### Fuzzy Matching

Why?

*Account for typos*

**Levenshtein edit distance** (*tolerance*) accounts for:

+ *Substitition*: interstell**a**r -> interstell**e**r
+ *Insertions*: interstellar -> inters**s**tellar
+ *Deletions*: interstel**l**ar -> interstelar

Above examples have an edit distance of 1

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "fuzzy": {
            "title": {
                "value": "intrsteller",
                "fuzziness": 2
            }
        }
    }
}'
```
Above query has a misspelled character and a character has been left out. With an edit distance of 2, we should still be able to find the right documents with int**e**rstell**a**r

### Partial Matching

Find movies that start with Star

Using `prefix`

```
bin/curl 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "prefix": {
            "title": "star"
        }
    }
}'
```
Using `wildcard`

```
bin/curl 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "wildcard": {
            "title": "star*"
        }
    }
}'
```

Using `regexp`

```
bin/curl 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "regexp": {
            "title": "star*"
        }
    }
}'
```

### Search As You Type

Use `match_phrase_prefix` and `slop`

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match_phrase_prefix": {
            "title": {
                "query": "star trek",
                "slop": 10
            }
        }
    }
}'
```

### N-Grams

Delete index and create a new one with N-Grams

`bin/curl -XDELETE 127.0.0.1:9200/movies`

+ Create new index with a filter called *autocomplete_filter*.
+ Create an analyzer called *autocomplete* which makes use of the *autocomplete_filter*

```
bin/curl -XPUT 127.0.0.1:9200/movies?pretty -d '
{
    "settings": {
        "analysis": {
            "filter": {
                "autocomplete_filter": {
                    "type": "edge_ngram",
                    "min_gram": 1,
                    "max_gram": 20
                }
            },
            "analyzer": {
                "autocomplete": {
                    "type": "custom",
                    "tokenizer": "standard",
                    "filter": [
                        "lowercase",
                        "autocomplete_filter"
                    ]
                }
            }
        }
    }
}'
```

Test the analyzer

```
bin/curl -XGET 127.0.0.1:9200/movies/_analyze?pretty -d '
{
    "analyzer": "autocomplete",
    "text": "Sta"
}'
```

Apply analyzer to title field

```
bin/curl -XPUT 127.0.0.1:9200/movies/_mapping?pretty -d '
{
    "properties": {
        "title": {
            "type": "text",
            "analyzer": "autocomplete"
        }
    }
}'
```

Index data

`bin/curl -XPUT 127.0.0.1:9200/_bulk --data-binary @data/movies.json`

Try out the analyzer

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match": {
            "title": "sta"
        }
    }
}'
```

Query above will be split into n-grams and return a lot more than titles

+ 1-Gram [s,t,a]
+ 2-Gram [st,ta]
+ 3-Gram [Sta]

To fix that use a standard query analyzer

```
bin/curl -XGET 127.0.0.1:9200/movies/_search?pretty -d '
{
    "query": {
        "match": {
            "title": {
                "query":"sta",
                "analyzer": "standard"
            }
        }
    }
}'
```

We can use a different anayzer at index time and at query time

## Section 4: Importing data into Index

+ Standalone scripts using REST API
+ logstash & beats for streaming data from logs and many other sources into ES
+ In AWS lambda or kinesis firehose
+ kafka, spark, etc have add-ons for ES

### Script via JSON

Get data

`wget http://files.grouplens.org/datasets/movielens/ml-latest-small.zip`

Get python script

`wget http://media.sundog-soft.com/es7/MoviesToJson.py`

Script

```{python}
import csv
import re

csvfile = open('data/ml-latest-small/movies.csv', 'r')

reader = csv.DictReader( csvfile )
for movie in reader:
        print ("{ \"create\" : { \"_index\": \"movies\", \"_id\" : \"" , movie['movieId'], "\" } }", sep='')
        title = re.sub(" \(.*\)$", "", re.sub('"','', movie['title']))
        year = movie['title'][-5:-1]
        if (not year.isdigit()):
            year = "2016"
        genres = movie['genres'].split('|')
        print ("{ \"id\": \"", movie['movieId'], "\", \"title\": \"", title, "\", \"year\":", year, ", \"genre\":[", end='', sep='')
        for genre in genres[:-1]:
            print("\"", genre, "\",", end='', sep='')
        print("\"", genres[-1], "\"", end = '', sep='')
        print ("] }")
```

Run script 

`python3 MoviesToJson.py > data/moremovies.json`

Replace our existing index

`bin/curl -XDELETE 127.0.0.1:9200/movies`

Import data

`bin/curl -XPUT 127.0.0.1:9200/_bulk --data-binary @data/moremovies.json`

### Using Client Libraries

`pip3 install elasticsearch`

Get ratings script

`wget http://media.sundog-soft.com/es7/IndexRatings.py`

Run

`python3 IndexRatings.py`

Get tags script

`wget http://media.sundog-soft.com/es7/IndexTags.py`

### Logstash

Install 

```
sudo apt install openjdk-8-jre-headless
sudo apt-get update
sudo apt-get install logstash
```

#### Setup

Download sample apache access log files

`wget media.sundog-soft.com/es/access_log`

Create logstash conf file using built-in apache log filter

> /etc/logstash/conf.d/logstash.conf

```
input {
    file {
        path => "/disk1/workspaces/elasticsearch/udemy/data/access_log"
        start_position => "beginning"
    }
}
filter {
    grok {
        match => {"message" => "%{COMBINEDAPACHELOG}"}
       
    }
    date {
        match => ["timestamp", "dd/MMM/yyyy:HH:mm:ss Z"]
    }
}
output {
    elasticsearch {
        hosts => ["localhost:9200"]
    }
    stdout {
        codec => rubydebug
    }
}
```

#### Run logstash

```
cd /usr/share/logstash/
sudo bin/logstash -f /etc/logstash/conf.d/logstash.conf
```

We need to find the index that logstash created. To list the catalog of indices use the following command

`bin/curl -XGET 127.0.0.1:9200/_cat/indices?v`

Found *logstash-2019.06.19-000001*

Search data

`bin/curl -XGET 127.0.0.1:9200/logstash-2019.06.19-000001/_search?pretty`

#### Logstash with mysql 

Install MySql via docker

```
docker pull mysql/mysql-server:5.7
```

Get dataset for my sql

`wget http://files.grouplens.org/datasets/movielens/ml-100k.zip && unzip ml-100k.zip`

Start MySQL and mount `u.item` into the tmp directory so that we can load it into the database. 

```
docker run --name=mysql5.7 -d \
-v /disk3/mysql5.7/data:/var/lib/mysql \
-v /disk1/workspaces/elasticsearch/udemy/data/ml-100k/u.item:/tmp/u.item \
-p 3306:3306 \
mysql/mysql-server:5.7 
```

##### Connect to mysql

A random password is created for the root user. The following command to retrieve it

`docker logs mysql5.7 2>&1 | grep GENERATED`

Run the following command to connect. Use the password from the previous step.

`docker exec -it mysql5.7 mysql -uroot -p`

Generete a new password for the root user

`ALTER USER 'root'@'localhost' IDENTIFIED BY 'password';`

Create database and movies table

```
CREATE DATABASE movielens;
CREATE TABLE movielens.movies (
    movieID INT PRIMARY KEY NOT NULL,
    title TEXT,
    releaseDate DATE
);
```

Load data

```
LOAD DATA LOCAL INFILE '/tmp/u.item' INTO TABLE movielens.movies FIELDS TERMINATED BY '|'
(movieID, title, @var3)
set releaseDate = STR_TO_DATE(@var3, '%d-%M-%Y');
```

View data

```
USE movielens;
SELECT * from movies WHERE title LIKE '%ter%';
```

Get connector

```
wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.16.zip \
&& unzip mysql-connector-java-8.0.16.zip \
&& rm  mysql-connector-java-8.0.16.zip
```

Create logstash conf for mysql

> /etc/logstash/conf.d/mysql.conf
```
input {
    jdbc {
        jdbc_connection_string => "jdbc:mysql://localhost:3306/movielens"
        jdbc_user => "student"
        jdbc_password => "password"
        jdbc_driver_library => "/disk1/workspaces/elasticsearch/udemy/tools/mysql-connector-java-8.0.16/mysql-connector-java-8.0.16.jar"
        jdbc_driver_class => "com.mysql.jdbc.Driver"
        statement => "SELECT * FROM movies"
    }
}
output {
    stdout { 
        codec => json_lines
    }
    elasticsearch {
        hosts => ["localhost:9200"]
        index => "movielens-sql"
    }
}
```

setup test-user

```
CREATE USER 'student'@'172.17.0.1' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON *.* TO 'student'@'172.17.0.1';
```

Run logstash

```
cd /usr/share/logstash/
sudo bin/logstash -f /etc/logstash/conf.d/mysql.conf
```

Check data in elasticsearch

`bin/curl -XGET '127.0.0.1:9200/movielens-sql/_search?q=title:Star&pretty'`

#### Logstash

> /etc/logstash/conf.d/s3.conf

```
input {
    s3 {
        bucket => "some_bucket_name"
        access_key_id => "SOME ACCESS KEY ID"
        secret_access_key => "SOME SECRET ACCESS KEY"
    }
}
filter {
    grok {
        match => {"message" => "%{COMBINEDAPACHELOG}"}
       
    }
    date {
        match => ["timestamp", "dd/MMM/yyyy:HH:mm:ss Z"]
    }
}
output {
    elasticsearch {
        hosts => ["localhost:9200"]
        index => "s3-logs"
    }
    stdout {
        codec => rubydebug
    }
}
```

#### Logstash with Kafka

> /etc/logstash/conf.d/s3.conf

```
input {
    kafka {
        bootstrap_servers => "localhost:9092"
        topics => ["kafka-logs","shipping-app"]
    }
}
filter {
    grok {
        match => {"message" => "%{COMBINEDAPACHELOG}"}
       
    }
    date {
        match => ["timestamp", "dd/MMM/yyyy:HH:mm:ss Z"]
    }
}
output {
    elasticsearch {
        hosts => ["localhost:9200"]
        index => "kafka-logs"
    }
    stdout {
        codec => rubydebug
    }
}
```
#### Logstash with Apache Spark

> /etc/logstash/conf.d/apache-spark.conf

```
import org.elasticsearch.spark.sql._

...

val people = lines.map(mapper).toDF()

people.saveToEs("spark-people")
```

## Secton 5: Aggregations, Buckets and Metrics

### Aggregation
 
Bucket movie ratings by value

```
bin/curl -XGET '127.0.0.1:9200/ratings/_search?size=0&pretty' -d '
{
    "aggs": {
        "ratings": {
            "terms": {
                "field": "rating"
            }
        }
    }
}'
```

Get count of 5 star ratings

```
bin/curl -XGET '127.0.0.1:9200/ratings/_search?size=0&pretty' -d '
{
    "query": {
        "match": {
            "rating": 5.0
        }
    },
    "aggs": {
        "ratings": {
            "terms": {
                "field": "rating"
            }
        }
    }
}'
```

Get avarage rating for Star Wars Episode IV

```
bin/curl -XGET '127.0.0.1:9200/ratings/_search?size=0&pretty' -d '
{
    "query": {
        "match_phrase": {
            "title": "Star Wars Episode IV"
        }
    },
    "aggs": {
        "avg_rating": {
            "avg": {
                "field": "rating"
            }
        }
    }
}'
```

### Histograms

Count of movies in rating interval

```
bin/curl -XGET '127.0.0.1:9200/ratings/_search?size=0&pretty' -d '
{
    "query": {
        "match_phrase": {
            "title": "Star Wars Episode IV"
        }
    },
    "aggs": {
        "avg_rating": {
            "avg": {
                "field": "rating"
            }
        }
    }
}'
``` 

Bucket movies by whole star ratings..

```{bash}
bin/curl -XGET '127.0.0.1:9200/ratings/_search?size=0&pretty' -d '
{
    "aggs": {
        "whole_ratings": {
            "histogram": {
                "field": "rating",
                "interval": 1.0
            }
        }
    }
}'
```

Count movies by the decade they were released in

```{bash}
bin/curl -XGET '127.0.0.1:9200/movies/_search?size=0&pretty' -d '
{
    "aggs": {
        "release": {
            "histogram": {
                "field": "year",
                "interval": 10
            }
        }
    }
}'
```

### Timeseries

Website hits by hour

```{bash}
bin/curl -XGET '127.0.0.1:9200/kafka-logs/_search?size=0&pretty' -d '
{
    "aggs": {
        "timestamp": {
            "date_histogram": {
                "field": "@timestamp",
                "interval": "hour"
            }
        }
    }
}'
```

When Googlebot scrapes my site

```{bash}
bin/curl -XGET '127.0.0.1:9200/kafka-logs/_search?size=0&pretty' -d '
{
    "query": {
        "match": {
            "agent": "Googlebot
        }
    },
    "aggs": {
        "timestamp": {
            "date_histogram": {
                "field": "@timestamp",
                "interval": "hour"
            }
        }
    }
}'
```

### Nested Aggregation

Average rating for each star wars movie

Reindex data with type keyword for title to allow the aggragation that follows

```{bash}
bin/curl -XDELETE 127.0.0.1:9200/ratings

bin/curl -XPUT 127.0.0.1:9200/ratings -d '
{
    "mappings": {
        "properties": {
            "title": {
                "type": "text",
                "fielddata": true,
                "fields": {
                    "raw": {
                        "type": "keyword"
                    }
                }
            }
        }
    }
}'

```

```{bash}
bin/curl -XGET '127.0.0.1:9200/ratings/_search?size=0&pretty' -d '
{
    "query": {
        "match_phrase": {
            "title": "Star Wars"
        }
    },
    "aggs": {
        "titles": {
            "terms": {
                "field": "title.raw"
            },
            "aggs": {
                "avg_rating": {
                    "avg": {
                        "field": "rating"
                    }
                }
            }
        }
    }
}'
```

## Elasticsearch SQL

Describe movies

```{bash}
bin/curl -XPOST 127.0.0.1:9200/_xpack/sql?format=txt -d '
{
    "query": "DESCRIBE movies"
}'
```

Select first 10 titles from the movies index

```{bash}
bin/curl -XPOST 127.0.0.1:9200/_xpack/sql?format=txt -d '
{
    "query": "select title from movies limit 10"
}'
```

Select title and year from movies index where year is less than 1920 and order by year field

```{bash}
bin/curl -XPOST 127.0.0.1:9200/_xpack/sql?format=txt -d '
{
    "query": "select title, year from movies where year < 1920 order by year"
}'
```

Convert SQL to dsl by appending `/translate?pretty`

```{bash}
bin/curl -XPOST 127.0.0.1:9200/_xpack/sql/translate?pretty -d '
{
    "query": "select title, year from movies where year < 1920 order by year"
}'
```

### Elasticsearch sql cli

Run the following command to switch to elastic search sql cli.

```{bash}
sudo /./usr/share/elasticsearch/bin/elasticsearch-sql-cli
```

Type `quit` to exit
