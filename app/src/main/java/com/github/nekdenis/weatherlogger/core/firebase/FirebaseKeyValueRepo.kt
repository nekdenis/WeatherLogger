package com.github.nekdenis.weatherlogger.core.firebase

import com.github.nekdenis.weatherlogger.core.db.KeyValueRepo
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.stepango.koptional.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass

class FirebaseKeyValueRepo<K : Any, V : Any>(
        val db: FirebaseDatabase,
        val valClass: KClass<V>,
        val root: String,
        val rootRefHolder: FirebaseRootReferenceHolder,
        val rxFirebaseDatabase: RxFirebaseDatabase
) : KeyValueRepo<K, V> {

    override fun save(key: K, value: V, subkey: String): Single<V> {
        rxFirebaseDatabase.logger.d("${TAG} saving ${valClass.java.simpleName} key:: $key")
        val path = if (subkey.isBlank()) "$root/$key" else "$root/$subkey/$key"
        return taskToSingle(value,
                { rootRefHolder.rootReference.child(path).setValue(rxFirebaseDatabase.jsonMaker.toJsonMap(value)) }
        )
    }

    override fun remove(key: K): Completable = Completable.defer {
        taskToCompletable {
            rootRefHolder.rootReference
                    .child(key.toString())
                    .removeValue()
        }
    }

    override fun removeAll(): Completable = taskToCompletable { rootRefHolder.rootReference.removeValue() }

    override fun observe(key: K): Observable<Optional<V>> = Observable.defer {
        rxFirebaseDatabase.observeValue(Schedulers.io(), rootRefHolder.rootReference.child(key.toString()), valClass)
    }

    override fun remove(keys: Set<K>): Completable = rootRefHolder.rootReference.wrapTransaction { data ->
        keys.map { data.child(it.toString())?.value = null }
    }

    override fun save(data: Map<K, V>): Single<Map<K, V>> {
        rxFirebaseDatabase.logger.d("Firebase:: saving ${valClass.java.simpleName} ")
        return taskToSingle(data,
                { rootRefHolder.rootReference.setValue(rxFirebaseDatabase.jsonMaker.toJsonMap(data.mapKeys { it.key.toString() })) }
        )
    }

    override fun observeAll(): Observable<List<V>> = Observable.defer {
        rxFirebaseDatabase
                .observeValuesList(Schedulers.io(), rootRefHolder.rootReference.child(root), valClass)
                .onErrorResumeNext({ it: Throwable ->
                    if (it is DatabaseException) {
                        rxFirebaseDatabase.logger.e(it, it.message ?: "")
                        Observable.empty()
                    } else {
                        Observable.error(it)
                    }
                })
    }

    companion object {
        inline operator fun <reified K : Any, reified V : Any> invoke(
                db: FirebaseDatabase,
                root: String,
                rootRefHolder: FirebaseRootReferenceHolder,
                rxFirebaseDatabase: RxFirebaseDatabase
        ): FirebaseKeyValueRepo<K, V> = FirebaseKeyValueRepo(db, V::class, root, rootRefHolder, rxFirebaseDatabase)
    }
}