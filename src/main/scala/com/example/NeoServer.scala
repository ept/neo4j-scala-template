package com.example

import java.io.Serializable
import java.util.{TreeMap, Properties}
import org.neo4j.api.core.{EmbeddedNeo, NeoService}

/**
 * Wrapper around a singleton instance of Neo4j embedded server.
 */
object NeoServer {

  private var neo: NeoService = null

  /**
   * Initialize Neo4j with configuration from a properties file.
   */
  def startup(config: Properties) {
    if (neo != null) neo.shutdown
    neo = new EmbeddedNeo(config.getProperty("neo4j.path", "/tmp/neo4j"))
    
    // Setup shell if required
    val shell = config.getProperty("neo4j.shell.enabled", "false")
    if (Array("true", "yes", "1") contains shell.toLowerCase) {
      val shellProperties = new TreeMap[String, Serializable]
      try {
        shellProperties.put("port", Integer.parseInt(config.getProperty("neo4j.shell.port")))
      } catch {
        case _: NumberFormatException => // also catches getProperty == null
      }
      val shellName = config.getProperty("neo4j.shell.name")
      if (shellName != null) shellProperties.put("name", shellName)
      neo.enableRemoteShell(shellProperties)
    }
  }

  /**
   * Do a clean shutdown of Neo4j.
   */
  def shutdown {
    if (neo != null) neo.shutdown
    neo = null
  }

  /**
   * Execute instructions within a Neo4j transaction; rollback if exception is raised and
   * commit otherwise; and return the return value from the operation.
   */
  def exec[T<:AnyRef](operation: NeoService => T): T = {
    val tx = neo.beginTx
    try {
      val ret = operation(neo)
      tx.success
      return ret
    } finally {
      tx.finish
    }
  }
}
