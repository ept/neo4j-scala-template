package com.example.models

import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlRegistry

import org.neo4j.api.core.NeoService

/**
 * Example of using a model object which can be automatically serialized/unserialized
 * to/from JSON using JAXB. Unfortunately with this method you have to write a lot of
 * Java-esque boilerplate code, so unless you have existing JAXB objects you may find
 * it easier to do the JSON conversion explicitly, or use <tt>NeoJsonConverter</tt>
 * (see <tt>NeoResource</tt> for an example).
 */

/** A simple model of a cow. */
@XmlRootElement
class Moo(var colourOfSpots: java.lang.String) {

  /** Constructor which loads the colourOfSpots property from the Neo reference node */
  def this(neo: NeoService) =
    this(neo.getReferenceNode.getProperty("cowColour", "brown").asInstanceOf[String])

  /** Save this model object to Neo */
  def save(neo: NeoService) = neo.getReferenceNode.setProperty("cowColour", colourOfSpots)

  /** Zero-argument constructor is required */
  private [models] def this() = this(null.asInstanceOf[String])
  
  /** Sorry, JAXB expects Java-style getters and setters :-( */
  def getColourOfSpots = colourOfSpots
  def setColourOfSpots(c: String) { colourOfSpots = c }
}


/**
 * This is needed so that JAXB can find the Moo class.
 * <tt>ObjectFactory</tt> is a magic name.
 */
@XmlRegistry
class ObjectFactory {
  def createMoo = new Moo
}
