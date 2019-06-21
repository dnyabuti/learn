package com.github.dnyabuti.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) {
        CalculatorClient client = new CalculatorClient();
        client.run();

    }

    private void run(){

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        //doUnary(channel);
        //doServerStream(channel);
        //doClientStream(channel);
        //doBiDiStream(channel);
        doErrorCall(channel);

    }

    private void doUnary(ManagedChannel channel){
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest request = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(20)
                .build();
        stub.sum(request);

        SumResponse response = stub.sum(request);

        System.out.println(request.getFirstNumber() +" + "+request.getSecondNumber()+ " = " + response.getSumResult());

    }

    private void doServerStream(ManagedChannel channel){
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        // Streaming Server
        Long number = 18L;

        stub.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(number).build())
                .forEachRemaining(primeNumberDecompositionResponse -> {
                    System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
                });
        channel.shutdown();
    }

    private void doBiDiStream(ManagedChannel channel){
        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch =  new CountDownLatch(1);

        StreamObserver<FindMaximumRequest> requestObserver = stub.findMaximum(new StreamObserver<FindMaximumResponse>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Get new max from server: "+ value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending messages");
            }
        });

        Arrays.asList(23,343,12,23,4,12,66,45).forEach (
                number -> {
                    System.out.println ("Sending "+ number +" to server");
                    requestObserver.onNext(FindMaximumRequest
                            .newBuilder()
                            .setNumber(number)
                            .build());
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doClientStream(ManagedChannel channel){
        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch =  new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> computeAverageRequestStreamObserver = stub.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                // response from server
                System.out.println("Received a response from the server");
                System.out.println(value.getAverage());
                // onNext called only once
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                // server is done sending data
                System.out.println("The server has completed sending us data");
                latch.countDown();
            }
        });
//        System.out.println("Sending the first number: 40");
//        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest
//                .newBuilder()
//                .setNumber(40)
//                .build());
//
//        System.out.println("Sending the first number: 10");
//        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest
//                .newBuilder()
//                .setNumber(10)
//                .build());
//
//        System.out.println("Sending the first number: 10");
//        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest
//                .newBuilder()
//                .setNumber(10)
//                .build());
//
//        System.out.println("Answer should be 20");
        // stream 10000 messages to server (client streaming)
        for ( int i = 0; i < 10000; i++ ){
            computeAverageRequestStreamObserver.onNext(ComputeAverageRequest
                    .newBuilder()
                    .setNumber(i)
                    .build());
        }

        computeAverageRequestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doErrorCall(ManagedChannel channel){
        CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub = CalculatorServiceGrpc.newBlockingStub(channel);
        int number = -1;

        try {
            blockingStub.squareRoot(SquareRootRequest.newBuilder()
                    .setNumber(number)
                    .build());
        } catch (StatusRuntimeException e){
            System.out.println("Got an exception for square root!");
            e.printStackTrace();
        }
    }

}
