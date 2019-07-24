package com.github.dnyabuti.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

public class BlogServiceImpl  extends BlogServiceGrpc.BlogServiceImplBase{

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        System.out.println("Received create blog request");
        Blog blog = request.getBlog();

        Document doc = new Document("author_id",blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Insert blog...");
        // insert doc into mongodb
        collection.insertOne(doc);

        String id = doc.getObjectId("_id").toString();
        System.out.println("Blog with id: "+ id + " has been created");
        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {

        System.out.println("Read blog request for blog has been received");

        String blogId = request.getBlogId();

        System.out.println("Searching for blogId: "+ blogId);
        Document result = null;
        try {
            result = collection.find(Filters.eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e){
            System.out.println("Blog id "+ blogId + " not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("The blog with the corresponding id was not found")
                        .augmentDescription(e.getLocalizedMessage())
                        .asRuntimeException()
            );

        }

        if (result == null){
            System.out.println("Blog id "+ blogId + " not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                    .withDescription("The blog with the corresponding id was not found")
                    .asRuntimeException()
            );
        } else {

            System.out.println("Blog id "+ blogId + " found. Sending response");
            Blog blog = Blog.newBuilder()
                    .setAuthorId(result.getString("author_id"))
                    .setTitle(result.getString("title"))
                    .setContent(result.getString("content"))
                    .setId(blogId).build();

            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {

        Blog blog = request.getBlog();
        System.out.println("Received update blog request for blogId "+ blog.getId());

        System.out.println("Searching for blogId " + blog.getId() );

        Document result = null;
        try {
            result = collection.find(Filters.eq("_id", new ObjectId(blog.getId())))
                    .first();
        } catch (Exception e){
            System.out.println("Blog id "+ blog.getId() + " not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );

        }

        if (result == null){
            System.out.println("Blog id "+ blog.getId() + " not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {

            System.out.println("Blog id "+ blog.getId() + " found. Updating blog...");

            Document doc = new Document("author_id",blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent());

            UpdateResult updateResult = collection.replaceOne(Filters.eq("_id", new ObjectId(blog.getId())),doc);

            long numberOfUpdates = updateResult.getModifiedCount();

            if (numberOfUpdates != 1) {
                System.out.println("Update failed");
                responseObserver.onError(
                        Status.UNKNOWN
                                .withDescription("No records were updated")
                                .asRuntimeException()
                );
            }else {
                System.out.println("Blog updated. Sending response...");
                responseObserver.onNext(UpdateBlogResponse.newBuilder()
                        .setBlog(documentToBlog(doc).toBuilder().setId(blog.getId()))
                        .build());
                responseObserver.onCompleted();
            }
        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        System.out.println("Delete blog request for blog has been received");

        String blogId = request.getBlogId();

        System.out.println("Searching for blogId: "+ blogId);
        Document result = null;
        try {
            result = collection.find(Filters.eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e){
            System.out.println("Blog id "+ blogId + " not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );

        }

        if (result == null){
            System.out.println("Blog id "+ blogId + " not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {

            System.out.println("Deleting blogId "+ blogId);
            long deleteCount = collection.deleteOne(
                    Filters.eq("_id", new ObjectId(blogId)))
                    .getDeletedCount();

            if (deleteCount != 1) {
                System.out.println("Delete operation failed. Expected to delete 1 record but "+ deleteCount + " records were deleted");
                responseObserver.onError(
                        Status.UNKNOWN
                                .withDescription("Delete operation failed. Expected to delete 1 record but "+ deleteCount + " records were deleted")
                                .asRuntimeException()
                );
            }

            responseObserver.onNext(DeleteBlogResponse.newBuilder().setBlogId(blogId).build());
            responseObserver.onCompleted();
        }
    }

    private Blog documentToBlog(Document document){
        return  Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content")).build();
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("Received list blog req");

        collection.find().iterator().forEachRemaining(
                document -> responseObserver.onNext(ListBlogResponse.newBuilder()
                        .setBlog(documentToBlog(document))
                        .build()
                ));

        responseObserver.onCompleted();

    }
}
