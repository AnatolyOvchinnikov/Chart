package com.app.chartapplication.model.interactor

import com.app.chartapplication.entity.ServerResponse
import com.app.chartapplication.model.repository.ChartRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
class ChartInteractor @Inject constructor(var repository: ChartRepository) {
    fun requestCoordsList(count: Int): Single<ServerResponse> {
        return repository.requestCoordsList(count)
            .map {
                val response = it.response.copy(points = it.response.points.sortedBy {
                    it.x
                })
                return@map it.copy(response = response)
            }
    }
}