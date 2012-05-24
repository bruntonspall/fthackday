package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import model.FTStoryCollector
import play.api.cache.Cache
import play.api.libs.concurrent.Promise

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {
  
  "Application" should {
    
    "send 404 on a bad request" in {
      running(FakeApplication()) {
        routeAndCall(FakeRequest(GET, "/boum")) must beNone        
      }
    }
    
    "cache response if available" in {
      running(FakeApplication()) {
        import play.api.Play.current

        Cache.get("foo") should be (None)
        FTStoryCollector.getOrElse("foo"){
          Promise.pure(Right("foo"))
        }
        Cache.get("foo") should be equalTo (Some("foo"))
      }
    }
    "not cache response if none" in {
      running(FakeApplication()) {
        import play.api.Play.current

        Cache.get("foo") should be (None)
        FTStoryCollector.getOrElse("foo"){
          Promise.pure(Left("x"))
        }
        Cache.get("foo") should be equalTo (None)
      }
    }
  }
}