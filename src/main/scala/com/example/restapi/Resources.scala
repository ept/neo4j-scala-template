package com.example.restapi

import com.sun.jersey.api.json.{JSONConfiguration, JSONJAXBContext}
import javax.ws.rs.ext.{ContextResolver, Provider}
import javax.xml.bind.JAXBContext

/**
 * Defines the conversion of bean objects to/from JSON. You only need this if you
 * are using JAXB for JSON serialisation/unserialisation, like in <tt>MooResource</tt>.
 */
@Provider
class JAXBContextResolver extends ContextResolver[JAXBContext] {
  private val context = new JSONJAXBContext(JSONConfiguration.natural.build, "com.example.models")
  override def getContext(objectType: Class[_]): JAXBContext = context
}
