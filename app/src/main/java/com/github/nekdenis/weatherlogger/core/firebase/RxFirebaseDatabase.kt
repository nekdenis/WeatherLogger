package com.github.nekdenis.weatherlogger.core.firebase

import com.github.nekdenis.weatherlogger.core.network.json.JsonMaker
import com.github.nekdenis.weatherlogger.core.system.Logger
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.stepango.koptional.Optional
import com.stepango.koptional.toOptional
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import kotlin.reflect.KClass

internal const val TAG = "Firebase::"

class RxFirebaseDatabase(
        val jsonMaker: JsonMaker,
        val logger: Logger
) {

    fun <T : Any> observeValue(scheduler: Scheduler, query: Query, clazz: KClass<T>): Observable<Optional<T>> {
        logger.d("${TAG} Observe value of class ${clazz.java.simpleName} $query")
        return Observable.create<Optional<T>> { emitter ->
            var scheduled: Disposable? = null
            val valueEventListener = valueEventListener(emitter) { dataSnapshot ->
                scheduled = scheduler.createWorker().schedule {
                    if (!emitter.isDisposed) try {
                        val json = dataSnapshot.value
                        json?.let {
                            val value = jsonMaker.fromJson(json as Map<*, *>, clazz)
                            logger.d("${TAG} Emmit value update event: $query")
                            emitter.onNext(value.toOptional())
                        } ?: emitter.onNext(Optional.empty())
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
            emitter.setCancellable {
                query.removeEventListener(valueEventListener)
                scheduled?.dispose()
            }
            query.addValueEventListener(valueEventListener)
        }
    }

    fun <T : Any> observeValuesList(scheduler: Scheduler, query: Query, clazz: KClass<T>): Observable<List<T>> {
        logger.d("${TAG} observe values list of ${clazz.java.simpleName} $query")
        return Observable.create<List<T>> { emitter ->
            var scheduled: Disposable? = null
            val valueEventListener = valueEventListener(emitter) { dataSnapshot ->
                scheduled = scheduler.createWorker().schedule {
                    if (!emitter.isDisposed) try {
                        val items = dataSnapshot.children
                                .map { it.value }
                                .map { jsonMaker.fromJson(it as Map<*, *>, clazz) }
                                .fold(mutableListOf<T>()) { list, item -> list.apply { add(item) } }
                        if (!emitter.isDisposed) {
                            emitter.onNext(items)
                            logger.d("${TAG} Emmit value update event: $query")
                        }
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
            emitter.setCancellable {
                query.removeEventListener(valueEventListener)
                scheduled?.dispose()
            }
            query.addValueEventListener(valueEventListener)
        }
    }
}

inline fun valueEventListener(
        subscriber: ObservableEmitter<*>,
        crossinline onDataChangedBlock: (DataSnapshot) -> Unit
) = object : ValueEventListener {
    override fun onCancelled(error: DatabaseError)
            = if (!subscriber.isDisposed) subscriber.onError(error.toException()) else Unit

    override fun onDataChange(dataSnapshot: DataSnapshot) = onDataChangedBlock(dataSnapshot)
}
