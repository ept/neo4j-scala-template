package com.example.restapi

import javax.ws.rs._
import javax.ws.rs.core._

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

