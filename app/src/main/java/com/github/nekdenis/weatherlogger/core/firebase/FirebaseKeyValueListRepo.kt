package com.github.nekdenis.weatherlogger.core.firebase

import com.github.nekdenis.weatherlogger.core.db.KeyValueListRepo
import com.github.nekdenis.weatherlogger.core.network.json.JsonMaker
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass

class FirebaseKeyValueListRepo<in K : Any, V : Any>(
        val db: FirebaseDatabase,
        val valClass: KClass<V>,
        val root: String,
        val rootRefHolder: FirebaseRootReferenceHolder,
        val rxFirebaseDatabase: RxFirebaseDatabase,
        val jsonMaker: JsonMaker
) : KeyValueListRepo<K, V> {

    override fun save(key: K, value: List<V>): Single<List<V>>
            = taskToSingle(value,
            { rootRefHolder.rootReference.child(key.toString()).setValue(jsonMaker.toJsonListMap(value)) }
    )

    override fun removeAll(): Completable = taskToCompletable { rootRefHolder.rootReference.removeValue() }

    override fun remove(key: K): Completable = taskToCompletable {
        rootRefHolder.rootReference
                .child(key.toString())
                .removeValue()
    }

    override fun observe(key: K): Observable<List<V>> = Observable.defer {
        rootRefHolder.rootReference
                .child(key.toString())
                .let { reference -> rxFirebaseDatabase.observeValuesList(Schedulers.io(), reference, valClass) }
    }

    companion object {
        inline operator fun <reified K : Any, reified V : Any> invoke(
                db: FirebaseDatabase,
                root: String,
                rootRefHolder: FirebaseRootReferenceHolder,
                rxFirebaseDatabase: RxFirebaseDatabase,
                jsonMaker: JsonMaker
        ): FirebaseKeyValueListRepo<K, V> = FirebaseKeyValueListRepo(db, V::class, root, rootRefHolder, rxFirebaseDatabase, jsonMaker)
    }
}