package model

import org.joda.time.DateTime

case class AbstractStory(headline: String, tags:Seq[String], publicationDate: DateTime)