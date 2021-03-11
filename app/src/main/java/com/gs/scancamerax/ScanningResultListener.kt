package com.gs.scancamerax

interface ScanningResultListener {
    fun onScanned(result: String)
    fun close()
}