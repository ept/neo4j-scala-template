package com.example.models

import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlRegistry

@XmlRegistry
class ObjectFactory {
  def createMoo = new Moo()
}

@XmlRootElement
class Moo(var colourOfSpots: java.lang.String) {
  private [models] def this() = this(null)
  
  def getColourOfSpots = colourOfSpots
  def setColourOfSpots(c: String) { colourOfSpots = c }
}
