package uz.alimov.gyroscopecircle.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.fragment.app.Fragment
import uz.alimov.gyroscopecircle.databinding.FragmentGyroscopeBinding
import kotlin.math.max
import kotlin.math.min

class GyroscopeFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentGyroscopeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null
    private var isGyroscopeEnabled = false
    private var circleAnimator: ViewPropertyAnimatorCompat? = null
    private val handler = Handler(Looper.getMainLooper())
    private val centerX: Float by lazy { binding.root.width / 2f - binding.circleView.width / 2f }
    private val centerY: Float by lazy { binding.root.height / 2f - binding.circleView.height / 2f }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGyroscopeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener to toggle gyroscope
        binding.circleView.setOnClickListener {
            isGyroscopeEnabled = !isGyroscopeEnabled
            if (isGyroscopeEnabled) {
                startGyroscope()
            } else {
                resetCirclePositionSmoothly()
            }
        }

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    private fun startGyroscope() {
        gyroscopeSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun resetCirclePositionSmoothly() {
        // Smoothly move the circle back to the center when gyroscope is off
        circleAnimator = ViewCompat.animate(binding.circleView)
            .x(centerX)
            .y(centerY)
            .setDuration(500)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (isGyroscopeEnabled) {
                val deltaX = it.values[1] * 50 // Scale movement for smoother response
                val deltaY = it.values[0] * 50

                val newX = binding.circleView.x + deltaX
                val newY = binding.circleView.y + deltaY

                // Ensure the circle doesn't move off-screen
                val constrainedX =
                    min(max(newX, 0f), binding.root.width - binding.circleView.width.toFloat())
                val constrainedY =
                    min(max(newY, 0f), binding.root.height - binding.circleView.height.toFloat())

                handler.post {
                    binding.circleView.x = constrainedX
                    binding.circleView.y = constrainedY
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onResume() {
        super.onResume()
        if (isGyroscopeEnabled) {
            startGyroscope()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}