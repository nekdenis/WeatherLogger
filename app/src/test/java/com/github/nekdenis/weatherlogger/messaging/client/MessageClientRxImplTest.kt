package com.github.nekdenis.weatherlogger.messaging.client

import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit


class MessageClientRxImplTest {
    var v = 0

    @Test
    fun onStart() {

        val s = Observable.create<Int> { emitter ->
            emitter.onNext(++v)
        }
        s.publish().connect()
        Assert.assertEquals(1, v)
    }

    @Test
    fun testRetry() {

//        Observable.create<Int> { s ->
//            println("subscribing")
//            s.onError(RuntimeException("always fails"))
//        }.retryWhen({ attempts ->
//            attempts.zipWith(Observable.range(1, 10), BiFunction<Throwable, Int, Int> { t1, t2 -> t2 })
//                    .flatMap({ i ->
//                        println("delay retry by $i second(s)")
//                        Observable.timer(i.toLong(), TimeUnit.SECONDS)
//                    })
//        }).blockingForEach { System.out.println() }

                var int = 0

                Observable.fromCallable {
                    int += 1
                    int
                }.doOnNext { System.out.println(it) }
                        .doOnNext { if (it < 4) throw Exception("aaa") }
                        .retryWhen { t -> t.delay (1000, TimeUnit.MILLISECONDS) }
                        .blockingForEach { System.out.println() }

    }

}