package com.example.restapi

import javax.ws.rs._
import javax.ws.rs.core._

import com.eptcomputing.neo4j.NeoServer
import com.example.models.Moo

/**
 * Example of a resource which uses a JAXB model object for the underlying logic and
 * defines its HTTP methods manually. It's only one of several possible ways of building
 * a domain-specific REST API around Neo4j -- see the other resource types in this
 * package for alternatives.
 */
@Path("/moo")
class MooResource {

  /**
   * <tt>POST /moo</tt> with a JSON document as body sets the most recently seen cow
   * colour to the colour specified in that document, and returns a JSON document
   * confirming the colour setting.
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def setMostRecentlySeenCow(cow: Moo) = {
    NeoServer.exec { neo => cow.save(neo) }
    cow
  }

  /**
   * <tt>GET /moo</tt> returns a description of the most recently seen cow as a JSON
   * document.
   */
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCow = NeoServer.exec { neo => new Moo(neo) }

}
