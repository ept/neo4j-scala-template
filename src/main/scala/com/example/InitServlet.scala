package com.example

import java.io.{InputStreamReader, IOException}
import java.util.Properties
import java.util.logging.Logger
import javax.servlet.http.HttpServlet

/**
 * This servlet is automatically initialized when the web app is started.
 * Here we set up the Neo4j server for the rest of the application to use.
 */
class InitServlet extends HttpServlet {
  val log = Logger.getLogger(this.getClass.getName) 

  override def init {
    log.info("Initializing Neo4J server.")
    // Load Neo4j configuration from properties file. You may want to replace this with something
    // more clever, e.g. loading different configs for development/staging/production environments.
    val neoConfig = new Properties
    try {
      val config = getServletContext.getResourceAsStream("/WEB-INF/neo4j.properties")
      neoConfig.load(new InputStreamReader(config))
    } catch {
      case e: IOException => log.info("Cannot read Neo4j configuration: " + e)
    }
    NeoServer.startup(neoConfig)
  }

  override def destroy {
    log.info("Shutting down Neo4J server.")
    NeoServer.shutdown
  }
}
