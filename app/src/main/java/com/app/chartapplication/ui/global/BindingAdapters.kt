package com.app.chartapplication.ui.global

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.chartapplication.entity.Coord
import com.app.chartapplication.ui.widget.ChartView

@BindingAdapter("bind:data")
fun <T> setData(recyclerView: RecyclerView, data: T) {
    if (recyclerView.adapter is BindableAdapter<*> && data != null && data is List<*>) {
        (recyclerView.adapter as BindableAdapter<T>).setData(data)
    }
}

@BindingAdapter("bind:chartData")
fun ChartView.setChartData(data: List<Coord>?) {
    if(data != null) {
        this.setValues(data.map { it.x }.toFloatArray(), data.map { it.y }.toFloatArray())
    }
}