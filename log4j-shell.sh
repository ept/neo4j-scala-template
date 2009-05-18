#!/bin/sh
# Simple script to open a Neo4j shell, connected to a running Neo4j server
# instance. To use it, you must set "neo4j.shell.enabled = true" in
# neo4j.properties.

java -jar "$HOME/.m2/repository/org/neo4j/shell/1.0-b8/shell-1.0-b8.jar"
