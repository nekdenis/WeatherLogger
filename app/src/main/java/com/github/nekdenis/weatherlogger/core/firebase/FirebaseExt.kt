package com.github.nekdenis.weatherlogger.core.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import io.reactivex.Completable
import io.reactivex.Single

inline fun DatabaseReference.wrapTransaction(
        crossinline block: (data: MutableData) -> Unit
): Completable = Completable.create { s ->
    runTransaction(object : Transaction.Handler {
        override fun onComplete(error: DatabaseError?, b: Boolean, data: DataSnapshot?) {
            error?.let { s.onError(it.toException()) }
                    ?: s.onComplete()
        }

        override fun doTransaction(data: MutableData) = try {
            block(data)
            Transaction.success(data)
        } catch (e: Exception) {
            Transaction.abort()
        }
    })
}

inline fun <T : Any> Task<T>.observe(crossinline onSuccess: () -> Unit, crossinline onError: (e: Throwable) -> Unit) {
    addOnSuccessListener { onSuccess() }
    addOnFailureListener { onError(it) }
}

fun taskToCompletable(block: () -> Task<*>): Completable = Completable.create { s ->
    block().observe(
            { if (!s.isDisposed) s.onComplete() },
            { if (!s.isDisposed) s.onError(it) }
    )
}

fun <T : Any> taskToSingle(value: T, block: () -> Task<*>): Single<T> = Single.create { s ->
    block().observe(
            { if (!s.isDisposed) s.onSuccess(value) },
            { if (!s.isDisposed) s.onError(it) }
    )
}
