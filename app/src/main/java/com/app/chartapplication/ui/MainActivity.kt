package com.app.chartapplication.ui

import android.app.ProgressDialog
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.chartapplication.R
import com.app.chartapplication.databinding.ActivityMainBinding
import com.app.chartapplication.extensions.hideKeyboard
import com.app.chartapplication.presentation.ChartViewModel
import com.app.chartapplication.ui.global.list.ChartAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ChartViewModel
    private lateinit var adapter: ChartAdapter
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this,
            com.app.chartapplication.R.layout.activity_main
        )
        setupObservers()
        adapter = ChartAdapter()

        val orientation = if(getResources().configuration.orientation == ORIENTATION_PORTRAIT) {
            LinearLayoutManager.HORIZONTAL
        } else {
            LinearLayoutManager.VERTICAL
        }

        recyclerView.layoutManager = LinearLayoutManager(this, orientation, false)
        recyclerView.adapter = adapter

        binding.lifecycleOwner = this
        binding.holder = this
        binding.viewModel = viewModel

        chartView.setReadyListener {
            viewModel.graphReady.postValue(true)
        }
        chartView.interpolate = viewModel.interpolate

        observeErrors()
        observeProgressDialog()
    }

    private fun observeErrors() {
        viewModel.errorMessage.observe(this, Observer {
            it?.let { error ->
                val message: String = when(error) {
                    ChartViewModel.UNKNOWN_ERROR -> getString(com.app.chartapplication.R.string.unknown_error)
                    ChartViewModel.EMPTY_COUNTER_ERROR -> getString(com.app.chartapplication.R.string.enter_value)
                    else -> error
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun observeProgressDialog() {
        viewModel.progressDialog.observe(this, Observer {
            it?.let { inProgress ->
                if(inProgress) {
                    showProgressDialog()
                } else {
                    hideProgressDialog()
                }
            }
        })
    }

    fun zoomInClick() {
        chartView.zoomIn()
    }

    fun zoomOutClick() {
        chartView.zoomOut()
    }

    fun switchClick() {
        viewModel.interpolate = !viewModel.interpolate
        chartView.interpolate = viewModel.interpolate
    }

    fun start() {
        hideKeyboard()
        viewModel.requestCoordsList()
    }

    fun saveImage() {
        MediaStore.Images.Media.insertImage(this.contentResolver, chartView.getBitmap(),
            System.currentTimeMillis().toString(), "")
        Toast.makeText(this, R.string.cart_saved, Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        viewModel = ViewModelProviders.of(this).get(ChartViewModel::class.java)
    }

    fun showProgressDialog() {
        if (progressDialog == null || progressDialog?.window == null || progressDialog?.isShowing == false) {
            progressDialog = ProgressDialog(this)
        }

        progressDialog?.let {
            if (!it.isShowing) {
                try {
                    it.show()
                    it.setCancelable(false)
                    it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    it.setContentView(com.app.chartapplication.R.layout.progressdialog_layout)
                } catch (e: WindowManager.BadTokenException) {

                }
            }
        }
    }

    fun hideProgressDialog() {
        if (progressDialog != null && progressDialog?.window != null) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }
}
