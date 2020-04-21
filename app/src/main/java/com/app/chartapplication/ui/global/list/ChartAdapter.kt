package com.app.chartapplication.ui.global.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.chartapplication.R
import com.app.chartapplication.databinding.CoordLayoutItemBinding
import com.app.chartapplication.entity.Coord
import com.app.chartapplication.ui.global.BindableAdapter

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
class ChartAdapter : RecyclerView.Adapter<ChartAdapter.ViewHolder>(),
    BindableAdapter<ArrayList<Coord>> {
    private val data = ArrayList<Coord>()

    override fun setData(data: ArrayList<Coord>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.coord_layout_item, parent, false))

    override fun getItemCount(): Int = this.data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(this.data[position])

    inner class ViewHolder(val binding: CoordLayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.executePendingBindings()
        }

        fun bind(coord: Coord) {
            binding.coord = coord
        }
    }
}