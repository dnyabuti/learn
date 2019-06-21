# GRPC Gateway for grpc project

### Steps

* Install dependencies as documented [https://github.com/grpc-ecosystem/grpc-gateway](https://github.com/grpc-ecosystem/grpc-gateway)
```
go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-grpc-gateway
go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-swagger
go get -u github.com/golang/protobuf/protoc-gen-go
```

* Generate gRPC stub
```
protoc -I/usr/local/include -I. \
  -I$GOPATH/src \
  -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
  --go_out=plugins=grpc:. \
  ./blog/blog.proto
```

* Generate reverse proxy
```
protoc -I/usr/local/include -I. \
  -I$GOPATH/src \
  -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
  --grpc-gateway_out=logtostderr=true:. \
  ./blog/blog.proto
```

* (Optional) Generate Swagger
```
protoc -I/usr/local/include -I. \
  -I$GOPATH/src \
  -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
  --swagger_out=logtostderr=true:. \
  ./blog/blog.proto
```

### Running

The [entrypoint](entry.go) has been provided. Assuming your server is already running on port 50051, run the following command

`go run entry.go`

> Test 
`curl -X GET -k "http://localhost:8080/blog/blogs"`

*expected output*
```
{"result":{"blog":{"author_id":"Davis","title":"Inception","content":"gRPC put RESTful APIs to rest."}}}
{"result":{"blog":{"author_id":"Davis","title":"Second Blog","content":"Micronaut + gRPC"}}}
...
```