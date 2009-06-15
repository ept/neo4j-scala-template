package com.example.restapi

import java.util.logging.Logger
import javax.ws.rs._

import com.eptcomputing.neo4j.NeoServer
import com.eptcomputing.neo4j.rest.RequiredParam
import com.example.models.Predicates

/**
 * Example of a completely manually defined RESTful resource. It gives you the greatest
 * flexibility but also produces fairly verbose code.
 */
@Path("/")
class ManualResource extends RequiredParam {

  @GET @Produces(Array("text/plain"))
  def pleaseDoPost = "To interact with this resource, please POST to it.\n"

  /**
   * <tt>POST /</tt> returns the string "Hello, brave Neo world!", assembled from two nodes
   * and a relationship.
   */
  @POST @Produces(Array("text/plain"))
  def helloWorld: String = {
    // Execute the following inside a Neo4j transaction
    NeoServer.exec(neo => {
      val firstNode = neo.createNode
      val secondNode = neo.createNode
      val relationship = firstNode.createRelationshipTo(secondNode, Predicates.KNOWS)
      firstNode.setProperty("message", "Hello, ")
      secondNode.setProperty("message", "world!\n")
      relationship.setProperty("message", "brave Neo ")
      firstNode.getProperty("message").toString + relationship.getProperty("message").toString +
        secondNode.getProperty("message").toString
    })
  }
}
