package com.app.chartapplication.model.repository

import com.app.chartapplication.entity.ServerResponse
import com.app.chartapplication.model.server.Api
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
@Singleton
class ChartRepository @Inject constructor(var api: Api) {

    fun requestCoordsList(count: Int): Single<ServerResponse> {
        return api.requestCoordsList(count, 1.1f)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}