// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: blog/blog.proto

package com.proto.blog;

public final class BlogOuterClass {
  private BlogOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_Blog_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_Blog_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_CreateBlogRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_CreateBlogRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_CreateBlogResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_CreateBlogResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_ReadBlogRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_ReadBlogRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_ReadBlogResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_ReadBlogResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_UpdateBlogRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_UpdateBlogRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_UpdateBlogResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_UpdateBlogResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_DeleteBlogRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_DeleteBlogRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_DeleteBlogResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_DeleteBlogResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_ListBlogRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_ListBlogRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_blog_ListBlogResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_blog_ListBlogResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017blog/blog.proto\022\004blog\032\034google/api/anno" +
      "tations.proto\"E\n\004Blog\022\n\n\002id\030\001 \001(\t\022\021\n\taut" +
      "hor_id\030\002 \001(\t\022\r\n\005title\030\003 \001(\t\022\017\n\007content\030\004" +
      " \001(\t\"-\n\021CreateBlogRequest\022\030\n\004blog\030\001 \001(\0132" +
      "\n.blog.Blog\".\n\022CreateBlogResponse\022\030\n\004blo" +
      "g\030\001 \001(\0132\n.blog.Blog\"\"\n\017ReadBlogRequest\022\017" +
      "\n\007blog_id\030\001 \001(\t\",\n\020ReadBlogResponse\022\030\n\004b" +
      "log\030\001 \001(\0132\n.blog.Blog\"-\n\021UpdateBlogReque" +
      "st\022\030\n\004blog\030\001 \001(\0132\n.blog.Blog\".\n\022UpdateBl" +
      "ogResponse\022\030\n\004blog\030\001 \001(\0132\n.blog.Blog\"$\n\021" +
      "DeleteBlogRequest\022\017\n\007blog_id\030\001 \001(\t\"%\n\022De" +
      "leteBlogResponse\022\017\n\007blog_id\030\001 \001(\t\"\021\n\017Lis" +
      "tBlogRequest\",\n\020ListBlogResponse\022\030\n\004blog" +
      "\030\001 \001(\0132\n.blog.Blog2\345\002\n\013BlogService\022A\n\nCr" +
      "eateBlog\022\027.blog.CreateBlogRequest\032\030.blog" +
      ".CreateBlogResponse\"\000\022;\n\010ReadBlog\022\025.blog" +
      ".ReadBlogRequest\032\026.blog.ReadBlogResponse" +
      "\"\000\022A\n\nUpdateBlog\022\027.blog.UpdateBlogReques" +
      "t\032\030.blog.UpdateBlogResponse\"\000\022A\n\nDeleteB" +
      "log\022\027.blog.DeleteBlogRequest\032\030.blog.Dele" +
      "teBlogResponse\"\000\022P\n\010ListBlog\022\025.blog.List" +
      "BlogRequest\032\026.blog.ListBlogResponse\"\023\202\323\344" +
      "\223\002\r\022\013/blog/blogs0\001B\022\n\016com.proto.blogP\001b\006" +
      "proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
        }, assigner);
    internal_static_blog_Blog_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_blog_Blog_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_Blog_descriptor,
        new java.lang.String[] { "Id", "AuthorId", "Title", "Content", });
    internal_static_blog_CreateBlogRequest_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_blog_CreateBlogRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_CreateBlogRequest_descriptor,
        new java.lang.String[] { "Blog", });
    internal_static_blog_CreateBlogResponse_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_blog_CreateBlogResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_CreateBlogResponse_descriptor,
        new java.lang.String[] { "Blog", });
    internal_static_blog_ReadBlogRequest_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_blog_ReadBlogRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_ReadBlogRequest_descriptor,
        new java.lang.String[] { "BlogId", });
    internal_static_blog_ReadBlogResponse_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_blog_ReadBlogResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_ReadBlogResponse_descriptor,
        new java.lang.String[] { "Blog", });
    internal_static_blog_UpdateBlogRequest_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_blog_UpdateBlogRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_UpdateBlogRequest_descriptor,
        new java.lang.String[] { "Blog", });
    internal_static_blog_UpdateBlogResponse_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_blog_UpdateBlogResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_UpdateBlogResponse_descriptor,
        new java.lang.String[] { "Blog", });
    internal_static_blog_DeleteBlogRequest_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_blog_DeleteBlogRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_DeleteBlogRequest_descriptor,
        new java.lang.String[] { "BlogId", });
    internal_static_blog_DeleteBlogResponse_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_blog_DeleteBlogResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_DeleteBlogResponse_descriptor,
        new java.lang.String[] { "BlogId", });
    internal_static_blog_ListBlogRequest_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_blog_ListBlogRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_ListBlogRequest_descriptor,
        new java.lang.String[] { });
    internal_static_blog_ListBlogResponse_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_blog_ListBlogResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_blog_ListBlogResponse_descriptor,
        new java.lang.String[] { "Blog", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
