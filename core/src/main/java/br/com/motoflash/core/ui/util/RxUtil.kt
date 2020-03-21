package br.com.motoflash.core.ui.util

import io.reactivex.CompletableTransformer
import io.reactivex.FlowableTransformer
import io.reactivex.MaybeTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler

/**
 * Created by Lucas Cordeiro on 23-Jun-17.
 */

object RxUtil {

    fun <T> applyNetworkSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it.subscribeOn(io)
                    .unsubscribeOn(io)
                    .observeOn(ui)
        }
    }

    fun applyCompletableNetworkSchedulers(): CompletableTransformer {
        return CompletableTransformer {
            it.subscribeOn(io)
                .unsubscribeOn(io)
                .observeOn(ui)
        }
    }

    fun <T> applyComputationSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it.subscribeOn(computation)
                    .unsubscribeOn(computation)
                    .observeOn(ui)
        }
    }

    fun <T> applyFlowableComputationSchedulers(): FlowableTransformer<T, T> {
        return FlowableTransformer {
            it.subscribeOn(computation)
                    .unsubscribeOn(computation)
                    .observeOn(ui)
        }
    }

    fun <T> applyFlowableNetworkSchedulers(): FlowableTransformer<T, T> {
        return FlowableTransformer {
            it.subscribeOn(io)
                    .unsubscribeOn(io)
                    .observeOn(ui)
        }
    }

    fun <T> applySingleNetworkSchedulers(): SingleTransformer<T, T> {
        return SingleTransformer {
            it.subscribeOn(io)
                    .unsubscribeOn(io)
                    .observeOn(ui)
        }
    }

    fun <T> applySingleComputationSchedulers(): SingleTransformer<T, T> {
        return SingleTransformer {
            it.subscribeOn(computation)
                    .unsubscribeOn(computation)
                    .observeOn(ui)
        }
    }

    fun <T> applyMaybeComputationSchedulers(): MaybeTransformer<T, T> {
        return MaybeTransformer {
            it.subscribeOn(computation)
                    .unsubscribeOn(computation)
                    .observeOn(ui)
        }
    }

    fun <T> applyMaybeNetworkSchedulers(): MaybeTransformer<T, T> {
        return MaybeTransformer {
            it.subscribeOn(io)
                    .unsubscribeOn(io)
                    .observeOn(ui)
        }
    }

    private val testScheduler = TestScheduler()
    private val io = Schedulers.io()
    private val computation = Schedulers.computation()
    private val ui = AndroidSchedulers.mainThread()
    fun <T> io(): ObservableTransformer<T, T> =
            ObservableTransformer {
                it.subscribeOn(io)
                        .unsubscribeOn(io)
                        .observeOn(io)
            }

    fun <T> ioMain(): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
        }

    fun <T> ioMaybe(): MaybeTransformer<T, T> =
            MaybeTransformer {
                it.subscribeOn(io)
                        .unsubscribeOn(io)
                        .observeOn(io)
            }

    fun <T> ioFlowable(): FlowableTransformer<T, T> =
            FlowableTransformer {
                it.subscribeOn(io)
                        .unsubscribeOn(io)
                        .observeOn(io)
            }

    fun <T> ioFlowableMain(): FlowableTransformer<T, T> =
        FlowableTransformer {
            it.subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
        }

    fun <T> ioSingle(): SingleTransformer<T, T> {
        return SingleTransformer {
            it.subscribeOn(io)
                    .unsubscribeOn(io)
                    .observeOn(io)
        }
    }
}
