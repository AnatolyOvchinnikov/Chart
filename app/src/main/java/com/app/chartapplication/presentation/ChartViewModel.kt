package com.app.chartapplication.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.chartapplication.App
import com.app.chartapplication.entity.Coord
import com.app.chartapplication.model.SingleLiveEvent
import com.app.chartapplication.model.interactor.ChartInteractor
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
class ChartViewModel : ViewModel() {
    val data = MutableLiveData<ArrayList<Coord>>()
    val errorMessage = SingleLiveEvent<String>()
    val progressDialog = SingleLiveEvent<Boolean>()
    val graphReady = MutableLiveData<Boolean>(false)
    val count = MutableLiveData<String>()
    var interpolate = false

    @Inject
    lateinit var interactor: ChartInteractor

    private val compositeDisposable = CompositeDisposable()

    init {
        App.component?.inject(this)
    }

    fun emulate() {
        graphReady.postValue(false)
        val valuesX = floatArrayOf(-100.0f, 1.0f, 2.0f, 3.0f, 4.0f, 6.0f)
        val valuesY = floatArrayOf(8.0f, -1.0f, 1.0f, 1.0f, 4.0f, 2.0f)
        val array = arrayListOf<Coord>()
        for (i in 0 until valuesX.size) {
            array.add(Coord(valuesX[i], valuesY[i]))
        }
        data.postValue(array)
    }

    fun requestCoordsList() {
        val count = this.count.value
        if(count.isNullOrBlank()) {
            errorMessage.postValue(EMPTY_COUNTER_ERROR)
        } else {
            interactor.requestCoordsList(count.toInt())
                .doOnSubscribe { progressDialog.postValue(true) }
                .doAfterTerminate { progressDialog.postValue(false) }
                .subscribe({
                if(it.result == 0) {
                    data.postValue(arrayListOf<Coord>().apply {
                        addAll(it.response.points)
                    })
                } else {
                    errorMessage.postValue(UNKNOWN_ERROR)
                }
            }, {
                it?.message?.let {
                    errorMessage.postValue(it)
                }
            }).apply { compositeDisposable.add(this) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
    companion object {
        const val UNKNOWN_ERROR = "unknown_error"
        const val EMPTY_COUNTER_ERROR = "empty_counter_error"
    }
}