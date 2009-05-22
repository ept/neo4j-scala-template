package com.example.restapi

import scala.collection.mutable.HashSet
import java.util.logging.Logger

import com.sun.jersey.api.json.{JSONConfiguration, JSONJAXBContext}
import javax.ws.rs.ext.{ContextResolver, Provider}
import javax.xml.bind.JAXBContext

import org.neo4j.api.core._
import org.codehaus.jettison.json.{JSONObject, JSONArray, JSONException}

/**
 * Defines the conversion of bean objects to/from JSON.
 */
@Provider
class JAXBContextResolver extends ContextResolver[JAXBContext] {
  private val context = new JSONJAXBContext(JSONConfiguration.natural.build, "com.example.models")
  override def getContext(objectType: Class[_]): JAXBContext = context
}

/**
 * Provides helpers for converting Neo4j nodes to/from JSON.
 */
object NeoJsonConverter {

  val log = Logger.getLogger(this.getClass.getName)

  /**
   * Serialises a Neo4j node into a JSON object of the following form:
   * <pre>{
   *   "_id": 1234,
   *   "property1": "value1",
   *   "property2": 42,
   *   "_out": {
   *     "KNOWS": [ {"_end": 1235, "property": "value"}, {"_end": 1236} ],
   *     "LIKES": {"_end": 1237}
   *   },
   *   "_in": {
   *     "KNOWS": {"_start": 1233}
   *   }
   * }</pre>
   */
  def neoToJson(node: Node): JSONObject = {
    val obj = new JSONObject
    obj.put("_id", node.getId)
    neoPropertiesToJson(node, obj)
    obj.put("_out", neoRelationshipsToJson(node, Direction.OUTGOING))
    obj.put("_in",  neoRelationshipsToJson(node, Direction.INCOMING))
    obj
  }

  /**
   * Adds all properties of a given node or relationship to a JSONObject in a
   * <tt>key:value</tt> style.
   */
  private def neoPropertiesToJson(container: PropertyContainer, obj: JSONObject) {
    for (key <- container.getPropertyKeys) {
      obj.put(key, container.getProperty(key))
    }
  }

  /**
   * Creates a JSON object whose keys are relationship types, and whose values are
   * JSON arrays of objects representing all the relationships of that type.
   */
  private def neoRelationshipsToJson(node: Node, direction: Direction): JSONObject = {
    val obj = new JSONObject
    for (rel <- node.getRelationships(direction)) {
      obj.accumulate(rel.getType.name, {
        val relObj = new JSONObject
        relObj.put(if (rel.getStartNode == node) "_end" else "_start", rel.getOtherNode(node).getId)
        neoPropertiesToJson(rel, relObj)
        relObj
      })
    }
    obj
  }


  /**
   * Parses properties and relationships out of a JSON object in the form as documented
   * on <tt>neoToJson</tt>. If <tt>existingNode</tt> is given, the properties and relationships
   * of that node are updated to match the JSON description; if null, a new node is created.
   * In either case, the up-to-date node is returned.
   */
  def jsonToNeo(json: JSONObject, neo: NeoService, existingNode: Node): Node = {
    val specialProps = Array("_id", "_url", "_in", "_out")
    val node = if (existingNode == null) neo.createNode else existingNode
    jsonPropertiesToNeo(json, node, specialProps)

    // Keep track of any of the node's relationships not present in the JSON
    val deleteRel = new HashSet[Long]
    for (rel <- node.getRelationships) deleteRel += rel.getId

    // Process special properties
    for (key <- specialProps) try {
      key match {
        case "_id"  =>
          if (json.getLong(key) != node.getId)
            log.warning("Mismatch of IDs: active node is " + node.getId + ", JSON specified " + json.getLong(key))

        case "_in"  =>
          val obj = json.getJSONObject(key)
          for (key <- obj.keys.asInstanceOf[java.util.Iterator[String]])
            jsonRelationshipToNeo(obj.get(key), neo, node, key, Direction.INCOMING, deleteRel)

        case "_out" =>
          val obj = json.getJSONObject(key)
          for (key <- obj.keys.asInstanceOf[java.util.Iterator[String]])
            jsonRelationshipToNeo(obj.get(key), neo, node, key, Direction.OUTGOING, deleteRel)
          
        case _ =>
      }
    } catch {
      case e: JSONException => log.warning("Error decoding " + key + " JSON property: " + e.getMessage)
    }

    // Delete any unused properties and relationships
    for (id <- deleteRel) neo.getRelationshipById(id).delete
    node
  }

  /**
   * Makes the properties of a Neo node or relationship match those in a given JSON object,
   * ignoring JSON object properties with particular names.
   */
  private def jsonPropertiesToNeo(json: JSONObject, container: PropertyContainer, ignoreProps: Seq[String]) {
    val deleteProp = new HashSet[String]
    for (key <- container.getPropertyKeys) deleteProp += key
    // Iterate over properties in JSON object
    for (key <- json.keys.asInstanceOf[java.util.Iterator[String]]) try {
      if (!(ignoreProps contains key)) {
        container.setProperty(key, json.get(key))
        deleteProp -= key
      }
    } catch {
      case e: JSONException => log.warning("Error decoding " + key + " JSON property: " + e.getMessage)
    }
    // Delete any properties not occurring in JSON object
    for (key <- deleteProp) container.removeProperty(key)
  }

  /** Tries to convert a single JSON object, representing a relationship, into Neo. */
  private def jsonRelationshipToNeo(obj: Object, neo: NeoService, node: Node, name: String,
                                    direction: Direction, unprocessedRels: HashSet[Long]) {
    val relType = DynamicRelationshipType.withName(name)

    def getOrCreate(otherNodeId: Long): Relationship = {
      // Try to find an existing relationship of the same type and direction between the two nodes
      val rel = node.getRelationships(relType, direction).find {
        rel => (rel.getOtherNode(node).getId == otherNodeId) && (unprocessedRels contains rel.getId)
      } getOrElse {
        // If no existing relationship was found, create a new one
        val other = neo.getNodeById(otherNodeId)
        if (direction == Direction.OUTGOING) {
          node.createRelationshipTo(other, relType)
        } else {
          other.createRelationshipTo(node, relType)
        }
      }
      unprocessedRels -= rel.getId // mark this relationship as used
      rel
    }

    // A relationship could be encoded in various different forms:
    try {
      obj match {
        case arr: JSONArray => // Array: process each element in turn
          for (n <- 0 until arr.length) jsonRelationshipToNeo(arr.get(n), neo, node, name, direction, unprocessedRels)

        case obj: JSONObject => // Object: must have _start or _end property, and maybe other properties
          val rel = getOrCreate(obj.getLong(if (direction == Direction.INCOMING) "_start" else "_end"))
          jsonPropertiesToNeo(obj, rel, Array("_id", "_url", "_start", "_end", "_type"))

        case num: java.lang.Number => // Number: just the ID of the other node
          getOrCreate(num.longValue)
      }
    } catch {
      case e: JSONException => log.warning("Error decoding " + direction + " " + name + " relationship from JSON: " +
                                           e.getMessage)
      case e: NotFoundException => log.warning("Reference to non-existent node in " + direction + " " + name +
                                               " relationship in JSON: " + e.getMessage)
    }
  }
                                     

  /** Implicitly convert a Java iterable to a Scala iterator. */
  implicit def java2scala[T](iter: java.lang.Iterable[T]): scala.Iterator[T] =
    new scala.collection.jcl.MutableIterator.Wrapper(iter.iterator)
  
  /** Implicitly convert a Java iterator to a Scala iterator. */
  implicit def java2scala[T](iter: java.util.Iterator[T]): scala.Iterator[T] =
    new scala.collection.jcl.MutableIterator.Wrapper(iter)
}
