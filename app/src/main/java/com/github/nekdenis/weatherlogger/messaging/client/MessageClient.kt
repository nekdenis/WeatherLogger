package com.github.nekdenis.weatherlogger.messaging.client

import com.github.nekdenis.weatherlogger.MQTT_RECONNECT_TIMEOUT
import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import com.github.nekdenis.weatherlogger.core.system.LCRX
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

interface MessageClient : LCRX {
    fun configClient(serverUrl: String, clientName: String, retryOnError: Boolean = true)
    fun observeConnection(): Observable<Boolean>
    fun subscribeToTopic(subscriptionTopic: String): Observable<String>
    fun publishMessage(publishMessage: String, publishTopic: String): Completable
}

private data class ClientMessage(val topic: String, val text: String)

private sealed class ClientEvents {
    class Connected : ClientEvents()
    class Disconnected : ClientEvents()
    class Received(val message: ClientMessage) : ClientEvents()
}

class MessageClientRxImpl(
        val client: MqttClient,
        val compositeDisposableHolder: CompositeDisposableHolder
) : MessageClient, CompositeDisposableHolder by compositeDisposableHolder {
    lateinit var serverUrl: String
    lateinit var clientName: String
    var retryOnError: Boolean = true

    private val clientObservable = Observable.create<ClientEvents> { emitter ->

        val onConnected = { emitter.onNext(ClientEvents.Connected()) }
        val onMessage = { topic: String, message: String -> emitter.onNext(ClientEvents.Received(ClientMessage(topic, message))) }
        val onErrorConnection = { e: Throwable -> emitter.onError(e) }

        emitter.setCancellable {
            client.disconnect()
        }
        client.connect(serverUrl, clientName, onConnected, onMessage, onErrorConnection)
    }.share().doOnSubscribe {  }

    override fun configClient(serverUrl: String, clientName: String, retryOnError: Boolean) {
        this.serverUrl = serverUrl
        this.clientName = clientName
        this.retryOnError = retryOnError
    }

    override fun onStart() {
        clientObservable
                .subscribeOn(Schedulers.io())
                .subscribe()
                .bind()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun subscribeToTopic(subscriptionTopic: String): Observable<String>
            = clientObservable.doOnSubscribe { client.subscribeToTopic(subscriptionTopic) }
            .filter { it is ClientEvents.Received && it.message.topic == subscriptionTopic }
            .map { it as ClientEvents.Received }
            .map { it.message.text }
            .observeOn(AndroidSchedulers.mainThread())

    override fun publishMessage(publishMessage: String, publishTopic: String): Completable = Completable.fromCallable {
        client.publishMessage(publishMessage, publishTopic)
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    override fun observeConnection(): Observable<Boolean>
            = clientObservable.filter { it is ClientEvents.Connected || it is ClientEvents.Disconnected }
            .map { it is ClientEvents.Connected }
            .apply {
                if (retryOnError) retryWhen { t -> t.delay(MQTT_RECONNECT_TIMEOUT, TimeUnit.MILLISECONDS) }
            }
}