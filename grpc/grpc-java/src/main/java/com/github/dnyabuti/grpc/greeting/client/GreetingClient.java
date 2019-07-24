package com.github.dnyabuti.grpc.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.opencensus.stats.Aggregation;

import javax.net.ssl.SSLException;
import java.io.File;
import java.sql.Time;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("Hello I am a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();
    }

    private void run() throws SSLException {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
//                .usePlaintext()
//                .build();

        // With server authentication SSL/TLS; custom CA root certificates; not on Android
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build()).build();

        System.out.println("Crating Stub");

         doUnaryCall(channel);
        // doSeverStreamingCall(channel);
        // doClientStreamingCall(channel);
        // doBiDiStreamingCall(channel);
        // doUnaryWithDeadline(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel){
        // DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        // DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // create a protocol buffer Greeting message;
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Davis")
                .setLastName("Nyabuti")
                .build();

        // create a protocol buffer GreetResponse
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();
        // call the RPC and get back a GreetResponse (protocol buffers)
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());
    }

    private void doSeverStreamingCall(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        // Server Streaming
        // create a protocol buffer Greeting message and Request;
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Davis").build())
                .build();
        // forEachRemaining sends continually until server sends onComplete
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }

    private void doClientStreamingCall(ManagedChannel channel){
        // create async client (stub)
        GreetServiceGrpc.GreetServiceStub aSyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch =  new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestStreamObserver = aSyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // response from server
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
                // onNext called only once
            }

            @Override
            public void onError(Throwable t) {
                // error from server
            }

            @Override
            public void onCompleted() {
                // server is done sending data
                System.out.println("The server has completed sending us data");
                latch.countDown();
            }
        });
        System.out.println("Sending message 1");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Davis")
                        .build())
                .build());
        System.out.println("Sending message 2");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Davis2")
                        .build())
                .build());
        System.out.println("Sending message 3");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Davis3")
                        .build())
                .build());

        // client is done sending data
        requestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

       StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
           @Override
           public void onNext(GreetEveryoneResponse value) {
               System.out.println("Response from server");
               System.out.println(value.getResult());
           }

           @Override
           public void onError(Throwable t) {
               latch.countDown();
           }

           @Override
           public void onCompleted() {
               System.out.println("Server is done sending data");
               latch.countDown();
           }
       });
        Arrays.asList("Davis", "Andrew", "Nancy", "Jude").forEach(
                name -> {
                    System.out.println("Sending: "+ name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder().setFirstName(name))
                            .build());
                    try {
                        // introduce artifical delay
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );
        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doUnaryWithDeadline(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // 5000 ms deadline
        try {
            System.out.println("Sending request with a deadline of 5000 ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(5000, TimeUnit.MILLISECONDS)).greetWithDeadline(
                    GreetWithDeadlineRequest.newBuilder().setGreeting(
                            Greeting.newBuilder().setFirstName("Davis").build()).build()
            );
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e){
            if (e.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded. Response is not needed");
            } else {
                e.printStackTrace();
            }

        }
        try {
            // 60 ms deadline
            System.out.println("Sending request with a deadline of 60 ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(60, TimeUnit.MILLISECONDS)).greetWithDeadline(
                    GreetWithDeadlineRequest.newBuilder().setGreeting(
                            Greeting.newBuilder().setFirstName("Davis").build()).build()
            );
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e){
            if (e.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded. Response is not needed");
            } else {
                e.printStackTrace();
            }

        }
    }
}
