package com.example.wifianalyser.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wifianalyser.utils.extensions.hide
import com.example.wifianalyser.utils.extensions.visible
import com.example.wifianalyser.utils.CheckPermission
import com.example.wifianalyser.models.AccessPointFilterEnum
import com.example.wifianalyser.models.BarChartDbmModel
import com.example.wifianalyser.models.LabelList
import com.example.wifianalyser.R
import com.example.wifianalyser.databinding.FragmentBarChartBinding
import com.example.wifianalyser.databinding.PermissionNotAllowBinding
import com.example.wifianalyser.ui.fragments.AccessPointsFragment.AccessPointsViewModel
import com.example.wifianalyser.ui.dialogs.PermissionDialog
import com.example.wifianalyser.ui.setting.UserSetting
import com.example.wifianalyser.ui.viewModel.UnRegisterReceiverViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class BarChartFragment : Fragment() {

    lateinit var chart: BarChart
    lateinit var barData: BarData
    lateinit var barDataSet: BarDataSet
    lateinit var barEntryList: ArrayList<BarEntry>
    lateinit var binding: FragmentBarChartBinding
    private val accessPointsViewModel by activityViewModels<AccessPointsViewModel>()
    private var scanPauseResumeTag: Boolean = true
    private val unregisterReceiverViewModel by activityViewModels<UnRegisterReceiverViewModel>()
    lateinit var permissionNotAllowBinding: PermissionNotAllowBinding
    lateinit var myColors: IntArray

    @Inject
    lateinit var permissionDialog: PermissionDialog
    lateinit var accessPointsSpeedList: ArrayList<BarChartDbmModel>
    private lateinit var labelList: ArrayList<LabelList>
    private var fiveGhztag: Boolean = false
    private var twoGhztag: Boolean = false
    var tempCount = 0
    var tempSpeed = 0
    var colorArraySize = 0
    var colorArrayIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBarChartBinding.inflate(inflater)

        MobileAds.initialize(requireActivity()) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.permissionNotAllow.setOnInflateListener { stub, inflated ->
            permissionNotAllowBinding = PermissionNotAllowBinding.bind(inflated)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListener()
        setObserver()

    }

    private fun initialize() {
        accessPointsSpeedList = ArrayList()
        barEntryList = ArrayList()
        labelList = ArrayList()
        chart = binding.barChart
        configureXAxis()
        configureYAxis()
        handleFilters()
        binding.permissionNotAllow.inflate()
        binding.toolbar.tvTitleFragment.text =
            resources.getString(com.example.wifianalyser.R.string.graph)
    }

    private fun setListener() {
        toolBarListener()
    }

    private fun setObserver() {

        accessPointsViewModel.accessPoints.observe(viewLifecycleOwner) { it ->
            barEntryList.clear()
            labelList.clear()
            tempCount = 0
            val tempList = ArrayList<Float>()
            it.map {
                tempCount++
                tempSpeed = it.dbm * -1
                accessPointsSpeedList.add(
                    BarChartDbmModel(
                        tempCount.toFloat(),
                        tempSpeed.toFloat()
                    )
                )
                if (!tempList.contains(tempSpeed.toFloat())) {
                    barEntryList.add(BarEntry(tempCount.toFloat(), tempSpeed.toFloat()))
                    labelList.add(LabelList(tempSpeed.toFloat(), it.accessPointName))
                    tempList.add(tempSpeed.toFloat())
                } else tempCount--

            }
            resetChart()
            setUpDataSet(barEntryList)
        }
    }

    private fun toolBarListener() {
        activity?.let { activity ->
            binding.toolbar.imgMore.setOnClickListener {
                startActivity(Intent(activity, UserSetting::class.java))
            }
            binding.toolbar.imgCapture.setOnClickListener {
                val imageUri = accessPointsViewModel.screenShotAndShare(activity, binding.root)
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/png"
                intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                startActivity(intent)
            }
            unregisterReceiverViewModel.receiverWatcher.observe(viewLifecycleOwner){
                scanPauseResumeTag = it
            }
            binding.toolbar.imgStartStopScan.setOnClickListener {
                when (scanPauseResumeTag) {
                    true -> {
                        unregisterReceiverViewModel.unRegister(false)
                    }
                    false -> {
                        unregisterReceiverViewModel.unRegister(true)
                    }
                }
            }
            unregisterReceiverViewModel.state.observe(viewLifecycleOwner) {
                binding.toolbar.imgStartStopScan.setImageResource(it)
            }
            permissionNotAllowBinding.btnPermission.setOnClickListener {
                permissionDialog.show(activity.supportFragmentManager, "permissionDialog")
            }
        }
    }

    private fun handlePermissions() {
        activity?.let {
            if (CheckPermission.verifyLocationPermission(it)) {
                binding.permissionNotAllow.hide()
                binding.clContent.visible()
            } else {
                binding.clContent.hide()
                binding.permissionNotAllow.visible()
            }
        }
    }

    private fun setUpDataSet(list: ArrayList<BarEntry>) {
        myColors = IntArray(list.size)
        colorArrayIndex = 0
        list.map {
            if (colorArrayIndex <= list.size) {
                when {

                    it.y <= 50 -> {
                        myColors[colorArrayIndex] = R.color.green_color
                        colorArrayIndex++

                    }
                    it.y > 50 && it.y <= 60 -> {
                        myColors[colorArrayIndex] = R.color.color_blue
                        colorArrayIndex++
                    }
                    it.y > 60 -> {
                        myColors[colorArrayIndex] = R.color.color_red
                        colorArrayIndex++
                    }
                    else -> {
                        myColors[colorArrayIndex] = R.color.color_red
                    }
                }
            }
        }

        barDataSet = BarDataSet(list, "Access Points")
        barDataSet.setColors(myColors, activity)
        if (barEntryList.isEmpty())
            Toast.makeText(activity, "No access point found!", Toast.LENGTH_SHORT).show()
        barDataSet.apply {
            valueFormatter = MyValueFormatter(labelList)
            Log.i("info", labelList.toString())
            valueTextSize = 10F
            barBorderWidth = 1f
        }

        barData = BarData(barDataSet)
        chart.setTouchEnabled(true)
        chart.isDragXEnabled = true
        chart.isScaleXEnabled = true
        chart.isScaleYEnabled = false
        chart.setVisibleXRangeMaximum(7f)
        chart.data = barData
    }

    private fun configureXAxis() {
        chart.xAxis.apply {
            axisMaximum = 16f
            axisMinimum = 0f
            position = XAxis.XAxisPosition.BOTTOM
            setLabelCount(16, true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toString().split(".")[0]
                }
            }
        }
    }

    private fun configureYAxis() {
        chart.axisRight.isEnabled = false

        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            textSize = 10f

            setLabelCount(11, true)
            chart.description = null

            activity?.let {
                textColor =
                    resources.getColor(R.color.black_and_white, it.theme)
            }

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(tempValue: Float): String {
                    val value: Int
                    when (tempValue) {
                        in 60f..100f -> {
                            tempValue.toInt()
                            value = tempValue.roundToInt() * -1
                            return "$value Poor"
                        }
                        in 50f..60f -> {
                            value = tempValue.roundToInt() * -1
                            return "$value Fair"
                        }
                        in 40f..50f -> {
                            value = tempValue.roundToInt() * -1
                            return "$value Strong"
                        }
                        in 30f..40f -> {
                            value = tempValue.roundToInt() * -1
                            return "$value Strong"
                        }
                        in 0f..30f -> {
                            value = tempValue.roundToInt() * -1
                            return "$value Strong"
                        }
                    }
                    value = tempValue.roundToInt() * -1
                    return value.toString()
                }
            }
        }
    }

    class MyValueFormatter(var entryList: ArrayList<LabelList>) : ValueFormatter() {

        override fun getFormattedValue(
            value: Float
        ): String {
            entryList.map {
                if (value == it.value)
                    return it.label
            }
            return "Hidden SSID"
        }
    }

    private fun resetChart() {
        chart.data?.clearValues()
        chart.notifyDataSetChanged()
        chart.clear()
        chart.invalidate()
    }

    private fun handleFilters() {
        binding.toolbar.img5ghz.apply {
            setOnClickListener {
                accessPointsViewModel.setFilter(AccessPointFilterEnum.FIVE_G)
                binding.toolbar.img24ghz.setImageResource(R.drawable.ic_twoghz_unactive)
                if (fiveGhztag)
                    accessPointsViewModel.setFilter(AccessPointFilterEnum.ALL)
                fiveGhztag = !fiveGhztag
                twoGhztag = false
                if (fiveGhztag) setImageResource(com.example.wifianalyser.R.drawable.ic_five_ghz_active)
                else setImageResource(R.drawable.ic_five_ghz_unactive)
            }
        }

        binding.toolbar.img24ghz.apply {
            setOnClickListener {
                accessPointsViewModel.setFilter(AccessPointFilterEnum.TWO_G)
                binding.toolbar.img5ghz.setImageResource(R.drawable.ic_five_ghz_unactive)
                if (twoGhztag)
                    accessPointsViewModel.setFilter(AccessPointFilterEnum.ALL)
                twoGhztag = !twoGhztag
                fiveGhztag = false
                if (twoGhztag) setImageResource(R.drawable.ic_twoghz_active)
                else setImageResource(R.drawable.ic_twoghz_unactive)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handlePermissions()
    }
}