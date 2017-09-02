package com.github.nekdenis.weatherlogger.core.firebase

import com.github.nekdenis.weatherlogger.core.db.SingleValueRepo
import com.google.firebase.database.FirebaseDatabase
import com.stepango.koptional.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.defer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass

class FirebaseSingleValueRepo<V : Any>(
        val db: FirebaseDatabase,
        val valClass: KClass<V>,
        val root: String,
        val rootRefHolder: FirebaseRootReferenceHolder,
        val rxFirebaseDatabase: RxFirebaseDatabase
) : SingleValueRepo<V> {

    override fun save(value: V): Single<V> = Single.create { s ->
        rxFirebaseDatabase.logger.d("${TAG} saving ${value::class.java.simpleName} to ${rootRefHolder.rootReference}")
        rootRefHolder.rootReference.child(root).setValue(rxFirebaseDatabase.jsonMaker.toJsonMap(value))
                .addOnSuccessListener { if (!s.isDisposed) s.onSuccess(value) }
                .addOnFailureListener { if (!s.isDisposed) s.onError(it) }
    }

    override fun remove(): Completable = Completable.create { s ->
        rootRefHolder.rootReference.removeValue { error, _ ->
            error?.let { s.onError(it.toException()) } ?: s.onComplete()
        }
    }

    override fun observe(): Observable<Optional<V>> = defer { rxFirebaseDatabase.observeValue(Schedulers.io(), rootRefHolder.rootReference.child(root), valClass) }

}

inline fun <reified T : Any> FirebaseSingleValueRepo(
        db: FirebaseDatabase,
        root: String,
        rootRefHolder: FirebaseRootReferenceHolder,
        rxFirebaseDatabase: RxFirebaseDatabase
) = FirebaseSingleValueRepo(db, T::class,root, rootRefHolder, rxFirebaseDatabase)