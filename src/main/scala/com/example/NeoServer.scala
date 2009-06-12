package com.example

import java.io.{FileInputStream, FileNotFoundException, InputStream, IOException, Serializable}
import java.util.{TreeMap, Properties}
import java.util.logging.Logger
import javax.servlet.ServletContext
import org.neo4j.api.core.{EmbeddedNeo, NeoService}

/**
 * Wrapper around a singleton instance of Neo4j embedded server.
 */
object NeoServer {

  private val log = Logger.getLogger(this.getClass.getName)
  private var neo: NeoService = null

  /**
   * Initialize Neo4j with configuration stored in a properties file specified via a
   * JVM system property, by using <code>-Dneo4j.config=/path/to/neo4j.properties</code>
   * on the command line.
   */
  def startup {
    val filename = System.getProperty("neo4j.config")
    if (filename == null) {
      log.warning("No Neo4j configuration file specified. Use -Dneo4j.config=/path/to/neo4j.properties")
    } else try {
      startup(new FileInputStream(filename))
    } catch {
      case _: FileNotFoundException => log.warning("Could not load Neo4j config from " + filename + ": file not found")
    }
  }

  /**
   * Initialize Neo4j with configuration stored in a properties file
   * <code>/WEB-INF/neo4j.properties</code> relative to the root of the given
   * servlet context.
   */
  def startup(context: ServletContext) {
    // Load Neo4j configuration from properties file. You may want to replace this with something
    // more clever, e.g. loading different configs for development/staging/production environments.
    val stream = context.getResourceAsStream("/WEB-INF/neo4j.properties")
    if (stream == null) {
      log.warning("Cannot read Neo4j configuration from /WEB-INF/neo4j.properties: resource not found")
    } else {
      startup(stream)
    }
  }

  /**
   * Initialize Neo4j with configuration loaded from an InputStream in Java properties
   * file format.
   */
  def startup(stream: InputStream) {
    val neoConfig = new Properties
    try {
      neoConfig.load(stream)
    } catch {
      case e: IOException => log.warning("Cannot read Neo4j configuration: " + e)
    }
    NeoServer.startup(neoConfig)
  }

  /**
   * Initialize Neo4j with configuration from a properties file.
   */
  def startup(config: Properties) {
    synchronized {
      if (neo != null) return
      log.info("Initializing Neo4j server")
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

      // Register a shutdown hook to ensure Neo4j is cleanly shut down before the JVM exits
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run() {
          NeoServer.shutdown
        }
      })
    }
  }

  /**
   * Do a clean shutdown of Neo4j.
   */
  def shutdown {
    synchronized {
      log.info("Shutting down Neo4j server")
      if (neo != null) neo.shutdown
      neo = null
    }
  }

  /**
   * Execute instructions within a Neo4j transaction; rollback if exception is raised and
   * commit otherwise; and return the return value from the operation.
   */
  def exec[T<:AnyRef](operation: NeoService => T): T = {
    val tx = synchronized {
      if (neo == null) startup
      neo.beginTx
    }
    try {
      val ret = operation(neo)
      tx.success
      return ret
    } finally {
      tx.finish
    }
  }
}
