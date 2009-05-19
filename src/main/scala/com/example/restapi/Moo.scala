package com.example.restapi

import java.util.logging.Logger
import javax.ws.rs._
import javax.ws.rs.core._

import com.example.NeoServer
import com.example.models._

@Path("/moo")
class MooResource extends RequiredParam {

  val log = Logger.getLogger(this.getClass.getName)
  
  def resourceURI(id: Int) = UriBuilder.fromResource(this.getClass).build(id.toString)

  /**
   * <tt>POST /moo</tt> with a JSON document as body creates a new entity based
   * on that document, and returns a JSON document of the form:
   * <pre>{"id": &lt;id of the new entity&gt;}</pre>
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def createJSON = {
    ""
  }

  /**
   * <tt>POST /moo</tt> with an URLencoded HTML form as body creates a new entity
   * based on that document, and issues a HTTP redirect to <tt>/moo/&lt;new id&gt;</tt>.
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_HTML))
  def createHTML = {
    val newId = 1
    Response.seeOther(resourceURI(newId)).build
  }

  /**
   * <tt>GET /moo/&lt;id&gt;</tt> returns a JSON representation of the entity with the
   * given ID. If the database contains a newer version of this entity, a HTTP redirect
   * to the newest version is issued (unless the query parameter <tt>?redirect=no</tt>
   * is given, in which case the old version is returned).
   */
  @GET @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def readJSON(@PathParam("id") id: String,
               @QueryParam("redirect") @DefaultValue("yes") redirect: String) = {
    requiredParam("id", id)
    //new Test("asdf", 1.0, 42)
    new Moo("brown")
  }

  /**
   * <tt>GET /moo/&lt;id&gt;</tt> returns a HTML representation of the entity with the
   * given ID. If the database contains a newer version of this entity, a HTTP redirect
   * to the newest version is issued (unless the query parameter <tt>?redirect=no</tt>
   * is given, in which case the old version is returned).
   */
  @GET @Path("/{id}")
  @Produces(Array(MediaType.TEXT_HTML))
  def readHTML(@PathParam("id") id: String,
               @QueryParam("redirect") @DefaultValue("yes") redirect: String) = {
    requiredParam("id", id)
    //log.info("Hello world invoked with hello=" + hello)
    NeoServer.exec(neo => {
      "moo"
    })
  }

  /**
   * <tt>PUT /moo/&lt;id&gt;</tt> with a JSON document as body updates the entity
   * with the given ID to new data, and returns a JSON document of the form:
   * <pre>{"id": &lt;id of the new entity&gt;}</pre>
   * If the database already contains a newer version of the
   * entity than the specified one, a HTTP 409 "Conflict" error is returned.
   */
  @PUT @Path("/{id}")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def updateJSON(@PathParam("id") id: String) = {
    requiredParam("id", id)
    ""
  }

  /**
   * <tt>PUT /moo/&lt;id&gt;</tt> with an URLencoded HTML form as body updates the entity
   * with the given ID to the new data, and issues a HTTP redirect to
   * <tt>/moo/&lt;new id&gt;</tt>. If the database already contains a newer version of the
   * entity than the specified one, a HTTP 409 "Conflict" error is returned.
   */
  @PUT @Path("/{id}")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_HTML))
  def updateHTML(@PathParam("id") id: String) = {
    requiredParam("id", id)
    val newId = 1
    Response.seeOther(resourceURI(newId)).build
  }

  /**
   * <tt>DELETE /moo/&lt;id&gt;</tt> marks the entity with the given ID as deleted.
   * If the database already contains a newer version of the
   * entity than the specified one, a HTTP 409 "Conflict" error is returned.
   */
  @DELETE @Path("/{id}")
  @Produces(Array(MediaType.WILDCARD))
  def delete(@PathParam("id") id: String) = {
    requiredParam("id", id)
    Response.ok.build
  }

  /**
   * Simulates <tt>PUT</tt> and <tt>DELETE</tt> methods for web browsers which do not
   * support them. The mapping is: <pre>
   *    POST /moo/&lt;id&gt;?method=put      --&gt;   PUT    /moo/&lt;id&gt;
   *    POST /moo/&lt;id&gt;?method=delete   --&gt;   DELETE /moo/&lt;id&gt;
   * </pre>
   */
  @POST @Path("/{id}")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_HTML))
  def simulatePutDelete(@PathParam("id") id: String, @QueryParam("method") method: String): Response = {
    method match {
      case "put" => updateHTML(id)
      case "delete" => delete(id)
      case _ => Response.status(Response.Status.BAD_REQUEST).entity("Method type not recognised").build
    }
  }
}
