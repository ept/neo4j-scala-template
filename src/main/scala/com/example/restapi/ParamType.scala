package com.example.restapi

import javax.ws.rs._
import javax.ws.rs.core._

import org.neo4j.api.core.{NeoService, Node, NotFoundException}

/**
 * Encapsulates the value of a parameter passed to the API (e.g. type of a @FormParam or
 * @QueryParam parameter). Subclass and implement the 'parse' method, throwing an exception
 * if invalid, and we'll turn it into a proper HTTP 'Bad Request' response.
 */
abstract class ParamType[V](param: String) {
  
  val value = try {
    parse(param)
  } catch {
    case e => throw new WebApplicationException(
      Response.status(Response.Status.BAD_REQUEST)
        .entity("Invalid value for " + paramName + ": " + param + " (" + e.getMessage() + ")").build
    )
  }
  
  def parse(param: String): V
  def paramName: String
  
  override def toString = value.toString
}


/**
 * Type for Neo4j a node referenced by ID.
 */
class NeoNodeParam(param: String) extends ParamType[Int](param) {
  def paramName = "node ID"

  def parse(param: String): Int = java.lang.Integer.parseInt(param)

  /**
   * Tries to find a node in a Neo server instance, raises HTTP 404 ("not found") if the node does
   * not exist.
   */
  def getNode(neo: NeoService): Node = {
    try {
      neo.getNodeById(value)
    } catch {
      case e: NotFoundException => throw new WebApplicationException(
        Response.status(Response.Status.NOT_FOUND)
          .entity("No node found with ID " + value).build
      )
    }
  }
}


/**
 * Provides a method which checks whether a parameter is present, and raises a proper HTTP
 * 'Bad Request' response if not.
 */
trait RequiredParam {
  def requiredParam(paramName: String, paramValue: Object) {
    if (paramValue == null) {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST)
          .entity("Required parameter " + paramName + " not given.").build
      )
    }
  }
}

