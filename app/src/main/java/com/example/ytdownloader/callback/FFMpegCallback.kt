package com.example.ytdownloader.callback

import java.io.File

interface FFMpegCallback {

    fun onProgress(progress: String)

    fun onSuccess(convertedFile: File, type: String)

    fun onFailure(error: Exception)

    fun onNotAvailable(error: Exception)

    fun onFinish()

}