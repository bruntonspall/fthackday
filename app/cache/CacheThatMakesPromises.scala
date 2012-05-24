package cache

import play.api.libs.concurrent.Promise
import play.api.cache.Cache
import play.api.Application

object CacheThatMakesPromises {
  def getOrElse[X, T](key: String)(fallback: => Promise[Either[X,T]])(implicit app: Application, m: Manifest[T]): Promise[Either[X,T]] = {
    Cache.getAs[T](key)(app, m) map (x => Promise.pure(Right(x))) getOrElse {
      val promise = fallback
      promise map { x => x match {
        case Right(v) => Cache.set(key, v)
        case _ =>
      }}
      promise
    }
  }
}
