package com.gs.scancamerax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gs.scancamerax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var scanBarcodeFragment: ScanBarcodeFragment?=null
    private lateinit var binding: ActivityMainBinding
    private var scanner:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btScan.setOnClickListener {
            scanner = !scanner
            setupScanner(scanner)
        }
    }



    private fun setupScanner(scanner: Boolean) {
        when (scanner) {
            true -> {
                scanBarcodeFragment = ScanBarcodeFragment.newInstance()
                scanBarcodeFragment?.setScanResultListener(object : ScanningResultListener {
                    override fun onScanned(result: String) {
                    }

                    override fun close() {

                    }
                })
                this.replaceFragment(
                    R.id.frameScannerFloating,
                    scanBarcodeFragment,
                    ScanBarcodeFragment.TAG
                )
            }
            false -> {
                deleteCameraScanner()
            }
        }
    }

    private fun deleteCameraScanner() {
        scanBarcodeFragment?.let {
            this.delete(R.id.frameScannerFloating, it)
        }
        scanBarcodeFragment = null
    }
}