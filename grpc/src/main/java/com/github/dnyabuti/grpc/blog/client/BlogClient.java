package com.github.dnyabuti.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    public static void main(String[] args) {
        BlogClient client = new BlogClient();
        client.run();

    }

    private void run(){
        System.out.println("Blog client");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub client = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Davis")
                .setTitle("Inception")
                .setContent("gRPC put RESTful APIs to rest.")
                .build();

        CreateBlogResponse response = client.createBlog(
                CreateBlogRequest.newBuilder()
                        .setBlog(blog)
                        .build());

        System.out.println("Received create blog response");
        System.out.println(response.toString());

        String blogId = response.getBlog().getId();

        // read the blog that was just created
        System.out.println("Reading blog that was just created");
        ReadBlogResponse readBlogResponse = client.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.toString());

        // read blog that does not exist
        try {
            System.out.println("Reading blog with non-existing id");
            ReadBlogResponse expectsNone = client.readBlog(ReadBlogRequest.newBuilder()
                    .setBlogId("fake_id")
                    .build());

            System.out.println(readBlogResponse.toString());
        } catch (Exception e){
            e.printStackTrace();
        }


        // update previous blog

        Blog blog2 = Blog.newBuilder()
                .setAuthorId("Davis")
                .setTitle("Second Blog")
                .setContent("Micronaut + gRPC")
                .setId(blogId)
                .build();

        System.out.println("Sending update blog request");

        UpdateBlogResponse updatedBlog = client.updateBlog(UpdateBlogRequest.newBuilder().setBlog(blog2).build());

        System.out.println("Updated blog:");
        System.out.println(updatedBlog.getBlog().toString());

        // delete blog
        System.out.println("Creating blog...");
        Blog blogToDelete = Blog.newBuilder()
                .setAuthorId("Davis")
                .setTitle("Inception")
                .setContent("gRPC put RESTful APIs to rest.")
                .build();

        response = client.createBlog(
                CreateBlogRequest.newBuilder()
                        .setBlog(blogToDelete)
                        .build());

        String delBlogId = response.getBlog().getId();

        System.out.println("Sending delete request for blog id " + delBlogId );

        DeleteBlogResponse deleteBlogResponse = client.deleteBlog(
                DeleteBlogRequest.newBuilder()
                        .setBlogId(delBlogId)
                        .build());

        System.out.println("Received delete blog response for blog id "+ deleteBlogResponse.getBlogId());


        // list all blog entries

        client.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString())
        );
    }

}
