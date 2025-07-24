package com.graytsar.batterynotification

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import com.graytsar.batterynotification.databinding.ActivityMainBinding
import com.scwang.wave.MultiWaveHeader
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val viewModel by viewModels<ViewModelAlarm>()

    private var model: BatteryEntity = BatteryEntity(0)

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            /* do nothing */
        }

    private lateinit var status: MaterialTextView
    private lateinit var health: MaterialTextView
    private lateinit var technology: MaterialTextView
    private lateinit var voltage: MaterialTextView
    private lateinit var capacity: MaterialTextView
    private lateinit var temperature: MaterialTextView
    private lateinit var plugged: MaterialTextView
    private lateinit var timeFull: MaterialTextView
    private lateinit var level: MaterialTextView
    private lateinit var seekBarMax: SeekBar
    private lateinit var seekBarMin: SeekBar
    private lateinit var enabledNotification: MaterialSwitch
    private lateinit var waveView: MultiWaveHeader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        viewModel.db.batteryDao().getModel()?.let {
            model = it
            viewModel.max.value = it.max
            viewModel.min.value = it.min
            viewModel.start.value = it.enabled
        } ?: let {
            val id = viewModel.db.batteryDao().insertModel(model)
            model = model.copy(id = id)
        }

        viewModel.max.value = model.max
        viewModel.min.value = model.min
        viewModel.start.value = model.enabled


        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(BatteryReceiver(), intentFilter)

        if (Build.VERSION.SDK_INT < 28) {
            binding.tableRowEstimated.visibility = View.GONE
        }

        initViews()
        initObservers()

        ViewCompat.setOnApplyWindowInsetsListener(binding.include.appbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onPause() {
        viewModel.db.batteryDao().updateModel(model)
        super.onPause()
    }

    private fun initViews() {
        status = binding.valStatus
        health = binding.valHealth
        technology = binding.valTechnology
        voltage = binding.valVoltage
        capacity = binding.valCapacity
        temperature = binding.valTemperature
        plugged = binding.valPlugged
        timeFull = binding.valEstimated
        level = binding.textLevel
        seekBarMax = binding.seekBarMax
        seekBarMin = binding.seekBarMin
        enabledNotification = binding.switchStart
        waveView = binding.waveView

        binding.seekBarMax.setOnSeekBarChangeListener(maxListener)
        binding.seekBarMin.setOnSeekBarChangeListener(minListener)
        binding.switchStart.setOnClickListener(onStartListener)
    }

    private fun initObservers() {
        viewModel.waveViewProgress.observe(this) { waveView.progress = it }
        viewModel.waveViewWaveHeight.observe(this) { waveView.waveHeight = it }
        viewModel.status.observe(this) { status.text = it }
        viewModel.health.observe(this) { health.text = it }
        viewModel.technology.observe(this) { technology.text = it }
        viewModel.voltage.observe(this) { voltage.text = it }
        viewModel.temperature.observe(this) { temperature.text = it }
        viewModel.estimated.observe(this) { timeFull.text = it }
        viewModel.level.observe(this) { level.text = it }
        viewModel.plugged.observe(this) { plugged.text = it }
        viewModel.capacity.observe(this) { capacity.text = it }
        viewModel.max.observe(this) {
            model = model.copy(max = it)
            seekBarMax.progress = it
        }
        viewModel.min.observe(this) {
            model = model.copy(min = it)
            seekBarMin.progress = it
        }
        viewModel.start.observe(this) {
            model = model.copy(enabled = it)
            enabledNotification.isChecked = it
        }
    }

    /**
     * Requests the notification permission.
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    /* do nothing */
                }

                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.all_notification)
                        .setMessage(R.string.notification_permission_rationale)
                        .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            dialogInterface?.dismiss()
                        }
                        .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                            dialogInterface?.dismiss()
                        }
                        .show()
                }

                else -> permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val onStartListener = View.OnClickListener { _: View? ->
        val isChecked = enabledNotification.isChecked
        val isNotificationPermissionGranted = PermissionUtil.checkNotificationPermission(this)
        if (isChecked && !isNotificationPermissionGranted) {
            requestNotificationPermission()
            enabledNotification.isChecked = false
        } else {
            viewModel.start.value = isChecked
        }
    }

    private val maxListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            viewModel.max.value = progress

            if (viewModel.max.value!! < viewModel.min.value!!) {
                viewModel.min.value = progress
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    private val minListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            viewModel.min.value = progress

            if (viewModel.min.value!! > viewModel.min.value!!) {
                viewModel.max.value = progress
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }
}
