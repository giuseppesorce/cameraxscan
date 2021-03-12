package com.gs.scancamerax


import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.gs.scancamerax.databinding.FragmentScanBarcodeBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

typealias BarcodeListener = (barcode: String) -> Unit


class ScanBarcodeFragment : Fragment() {
    private var scanningResultListener: ScanningResultListener? = null
    private var camera: Camera? = null
    private var processingBarcode = AtomicBoolean(false)
    private lateinit var cameraExecutor: ExecutorService
    private var _binding: FragmentScanBarcodeBinding? = null
    private val binding get() = _binding!!
    private val TAG = "CameraXBasic"
    private val RATIO_4_3_VALUE = 4.0 / 3.0
    private val RATIO_16_9_VALUE = 16.0 / 9.0
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanBarcodeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onResume() {
        super.onResume()
        processingBarcode.set(false)
        initFragment()
    }

    fun initFragment() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        if (allPermissionsGranted()) {
            binding.fragmentScanBarcodePreviewView.post {
                startCamera()
            }
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    private fun bindCameraUseCases() {
       var  metrics =
            DisplayMetrics().also { binding.fragmentScanBarcodePreviewView.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels ?: 0, metrics.heightPixels)
        val rotation = binding.fragmentScanBarcodePreviewView.display.rotation

        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector with default back camera
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { luma ->
                })
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
            preview?.setSurfaceProvider(binding.fragmentScanBarcodePreviewView.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

                binding.fragmentScanBarcodePreviewView.post {

                    startCamera()

                }

            } else {
                activity?.let {
                    Toast.makeText(
                        it.applicationContext,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun searchBarcode(barcode: String) {
        Log.e("driver", "searchBarcode: $barcode")
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        camera = null
        _binding = null
        super.onDestroy()
    }


    inner class BarcodeAnalyzer(private val barcodeListener: BarcodeListener) :
        ImageAnalysis.Analyzer {
        private var isBusy = AtomicBoolean(false)
        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(image: ImageProxy) {
            if (isBusy.compareAndSet(false, true)) {
                val visionImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
                BarcodeScanning.getClient().process(visionImage)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.let { result ->
                                binding.barcodeOverlay.update(visionImage.mediaImage, result)
                            }
                        } else {
                            Log.w("BarcodeAnalyzer", "failed to scan image: ${task.exception?.message}")
                        }
                        image.close()
                        isBusy.set(false)
                    }
            } else {
                image.close()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setScanResultListener(listener: ScanningResultListener) {
        this.scanningResultListener = listener
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val TAG = "BarCodeFragment"

        @JvmStatic
        fun newInstance() = ScanBarcodeFragment()
    }
}