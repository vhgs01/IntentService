package br.com.hugo.victor.intentservice

import android.os.Bundle
import android.os.ResultReceiver
import android.os.Handler

class DownloadResultReceiver(handler: Handler) : ResultReceiver(handler) {
    private var mReceiver: Receiver? = null

    fun setReceiver(receiver: Receiver) {
        mReceiver = receiver
    }

    interface Receiver {
        fun onReceiverResult(resultCode: Int, resultData: Bundle)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (mReceiver != null) {
            mReceiver!!.onReceiverResult(resultCode, resultData)
        }
    }
}