package com.example.restapi

import java.util.logging.Logger
import javax.ws.rs._

import com.example.NeoServer
import com.example.model.Predicates

@Path("/")
class Root extends RequiredParam {

  val log = Logger.getLogger(this.getClass.getName) 

  @GET @Produces(Array("text/html"))
  def helloWorld: String = {
    NeoServer.exec(neo => {
      val firstNode = neo.getReferenceNode
      val secondNode = neo.createNode
      val relationship = firstNode.createRelationshipTo(secondNode, Predicates.KNOWS)
      firstNode.setProperty("message", "Hello, ")
      secondNode.setProperty("message", "world!")
      relationship.setProperty("message", "brave Neo ")
      firstNode.getProperty("message").toString + relationship.getProperty("message").toString +
        secondNode.getProperty("message").toString               
    })
  }
}
