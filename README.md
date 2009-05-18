Neo4j/Scala/Jersey project template
===================================

* You want to develop an application using the [Neo4j](http://neo4j.org)
  open source graph database?
* You want to write your code in beautiful [Scala](http://www.scala-lang.org)?
* You want to expose your database through a nice clean REST API, implemented
  with [Jersey/JSR311]()?
* You want to get up and running quickly?

Then you've come to the right place. This repository contains a rough outline
for a new project, including a Maven descriptor `pom.xml` detailing all the
dependencies, an Eclipse project with all the right build settings, and
examples of how to write tests. You'll be up and running in no time.


How to use this template
------------------------

You need a Java 6 environment and Maven 2.0.9 or newer installed.

Download this repository, or clone it with `git`, or merge it into your own
git repository. You will probably want to
search the files for occurrences of `example` and replace them with something
more appropriate.

Out of the box, this template is set up to support the following goals:

* `mvn package` -- Compile, test and bundle into a `war` file for deployment to
  a standard Java web container (e.g. Tomcat).
* `mvn test` -- Just compile and test.
* `mvn glassfish:run` -- Compile and run the app in an embedded Glassfish server
  on `http://localhost:8080/neo4j-scala-template/`.
* `mvn clean` -- Remove build files

When you're developing using the embedded Glassfish instance, I suggest that you
keep another terminal open; whenever you want to reload, type `mvn compile` in
the terminal, then hit Enter in the terminal running Glassfish. That will reload
the application, enabling a fairly rapid development cycle. (It's still not great
though. If you have an idea to make it faster, please let me know.)


Useful links
------------

The main references you'll need for development:

* [Scala standard library reference](http://www.scala-lang.org/docu/files/api/index.html)
* Jersey Javadoc
* Neo4j Javadoc
* Scalatest


License
-------

This code is (c) 2009 Martin Kleppmann, and is distributed under the terms of
the BSD license. See the file LICENSE for details.
