# Influxdb

## Install influxdb

Get the config file...

```
docker run --rm influxdb influxd config > influxdb.conf
```

## Build container

```{bash}
docker build -t influxdb .
```

## Run

`docker run --name=influxdb -d -p 8086:8086 influxdb`

### Open shell to influxdb

`docker run --rm --link=influxdb -it influxdb influx -host influxdb`

Show databases

`show databases`

Import data

`docker run -v "$PWD":/tmp/testdata/ --rm --link=influxdb -it influxdb influx -import -path=/tmp/testdata/sample_data_course -host influxdb`

Use database

`use holiday_france`

Show measurements

`show measurements`

## Query Language

### SELECT

```{sql}
select * from speed_ms

select * from altitude_m limit 2;

select time, speed from speed_ms limit 2;

# select specific tags and fields
select "description"::field,"location"::tag,"altitude"::field from "altitude_m" limit 3;

#select all fields
select *::field from "altitude_m" limit 4;

#select field anf perform operation
select ("altitude"/1000) from "altitude_m" limit 3;

# select all fields from multiple measurements
select * from "altitude_m","speed_ms" limit 3;

```

### WHERE

```{sql}
select * from "altitude_m" where "altitude" > 194 limit 3;

select * from "speed_ms" where "speed description" = 'speed in meter per second' limit 3;

select * from "speed_ms" where "speed" + 1 > 12 limit 3;

select * from "speed_ms" where "speed" + 1 < 12 limit 3;

select * from "speed_ms" where "location" = 'biron_france' limit 3;

select * from "speed_ms" where "location" = 'biron_france' and ("speed" + 1 < 12 or "speed" + 1 > 13 )limit 10;
```

import data with the correct precision by passing `-precision s` in the import statement

```{bash}
docker run -v "$PWD":/tmp/testdata/ --rm --link=influxdb -it influxdb influx -precision s -import -path=/tmp/testdata/sample_data_course -host influxdb
docker run --rm --link=influxdb -it influxdb influx -host influxdb
```

```{sql}
select * from "speed_ms" where time > now() - 150d;

select * from "speed_ms" where time >= '2015-08-18';

select * from "speed_ms" where time >= '2000-08-18';

select * from "speed_ms" where time >= '1970-01-01';

select * from "speed_ms" where time > '2017-06-30T21:24:00Z' limit 3;

select * from "speed_ms" where time > now() - 100d limit 3;
```

### GROUP BY

Use NOAA_water_database database

```{bash}
docker run -v "$PWD":/tmp/testdata/ --rm --link=influxdb -it influxdb influx -precision s -import -path=/tmp/testdata/sample_data_course2 -host influxdb

# connect
docker run --rm --link=influxdb -it influxdb influx -host influxdb
```

```{sql}
use NOAA_water_database

Select mean("water_level") from "h2o_feet" group by "location"

select mean("water_level") from "h2o_feet" group by "location" limit 2

select mean("index") from "h2o_quality" group by * limit 2

select count("water_level") from "h2o_feet" where "location"='coyote_creek' and time >= '2015-08-18T00:00:00Z'
and time <= '2015-08-18T00:30:00Z' group by time(12m)
```

### INTO

```{sql}
SELECT * INTO "copy_speed_ms" FROM "speed_ms" GROUP BY *
```

#### Downsampling

```{sql}
SELECT MEAN("speed") INTO "speed_ms_downsample" FROM "speed_ms" WHERE "location"='biron_france' AND time >= '2017-08-08T16:15:46Z' AND time <= '2017-08-08T16:25:23Z' GROUP BY time(10s);
```

### SUBQUERIES

```{sql}
SELECT MEAN("water_level") AS "all_the_means" FROM "h2o_feet" WHERE time >= '2015-08-18T00:00:00Z' AND time <= '2015-08-18T00:30:00Z' GROUP BY time(12m)

SELECT SUM("water_level_derivative") AS "sum_derivative" FROM (SELECT DERIVATIVE(MEAN("water_level")) AS "water_level_derivative" FROM "h2o_feet" WHERE time >= '2015-08-18T00:00:00Z' AND time <= '2015-08-18T00:30:00Z' GROUP BY time(12m),"location") GROUP BY "location"
```

JOINS

**Joins don't exist in influxdb**.
