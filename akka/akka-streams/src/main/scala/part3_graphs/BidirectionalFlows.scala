package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}

object BidirectionalFlows extends App {

  implicit val system = ActorSystem("BidirectionalFlows")
  implicit val materializer = ActorMaterializer()

  // converting between 2 data types
  // cryptography

  def encrypt(n: Int)(string: String) = string.map(c => (c + n).toChar)
  def decrypt(n: Int)(string: String) = string.map(c => (c - n).toChar)

  // use bidiFlow
  val bidiCryptoStaticGraph = GraphDSL.create() { implicit builder =>
    val encryptFlowShape = builder.add(Flow[String].map(encrypt(3)))
    val decryptFlowShape = builder.add(Flow[String].map(decrypt(3)))

    //BidiShape(encryptFlowShape.in, encryptFlowShape.out, decryptFlowShape.in, decryptFlowShape.out)
    //better
    BidiShape.fromFlows(encryptFlowShape, decryptFlowShape)
  }

  val unencryptedStrings = List("Big", "data", "analytics", "beyond", "hadoop")
  val unencryptedStringsSource = Source(unencryptedStrings)

  val encryptedSource = Source(unencryptedStrings.map(encrypt(3)))

  val cryptoBidiGraph = RunnableGraph.fromGraph(
    GraphDSL.create() {implicit builder =>
      import GraphDSL.Implicits._

      val unencryptedSourceShape = builder.add(unencryptedStringsSource)
      val encryptedSourceShape = builder.add(encryptedSource)
      val bidi = builder.add(bidiCryptoStaticGraph)
      val encryptedSinkShape = Sink.foreach[String](string => println(s"Encrypted: $string"))
      val decryptedSinkShape = Sink.foreach[String](string => println(s"Decrypted: $string"))

      unencryptedSourceShape ~> bidi.in1 ; bidi.out1 ~> encryptedSinkShape
      decryptedSinkShape <~ bidi.out2 ; bidi.in2 <~ encryptedSourceShape

      ClosedShape
    }
  )

  cryptoBidiGraph.run()
}
