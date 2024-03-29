package com.cabe.app.novel.domain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cabe.app.novel.model.BaseObject
import com.cabe.lib.cache.CacheSource
import com.cabe.lib.cache.interactor.ViewPresenter
import com.cabe.lib.cache.interactor.impl.SimpleViewPresenter

data class DataError(val source: CacheSource, val code: Int, val msg: String?): BaseObject()
data class DataComplete(val source: CacheSource): BaseObject()
abstract class BaseViewModel<T>: ViewModel() {
    val liveResponse: MutableLiveData<T?> = MutableLiveData()
    val liveError: MutableLiveData<DataError?> = MutableLiveData()
    val liveComplete: MutableLiveData<DataComplete?> = MutableLiveData()

    open fun createPresenter(onComplete: (() -> Boolean)?= null): ViewPresenter<T> {
        return object : SimpleViewPresenter<T>() {
            override fun load(from: CacheSource, data: T) {
                liveResponse.postValue(data)
            }
            override fun error(from: CacheSource, code: Int, info: String) {
                liveError.postValue(DataError(from, code, info))
            }
            override fun complete(from: CacheSource) {
                super.complete(from)
                if(onComplete?.invoke() != false) {
                    liveComplete.postValue(DataComplete(from))
                }
            }
        }
    }
}