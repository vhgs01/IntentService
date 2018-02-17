package br.com.hugo.victor.intentservice

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DownloadResultReceiver.Receiver {

    lateinit var mReceiver: DownloadResultReceiver
    lateinit var arrayAdapter: ArrayAdapter<String>

    private val url = "https://pokeapi.co/api/v2/pokemon"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mReceiver = DownloadResultReceiver(Handler())
        mReceiver.setReceiver(this)

        val intent = Intent(Intent.ACTION_SYNC, null, this, DownloadService::class.java)
        intent.putExtra("url", url)
        intent.putExtra("receiver", mReceiver)

        startService(intent)
    }

    fun showProgress(visible: Boolean) {
        loading.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun preencherLista(resultData: Bundle) {
        showProgress(false)
        val results = resultData.getStringArray("result")

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, results)
        lvPokemons.adapter = arrayAdapter
    }

    override fun onReceiverResult(resultCode: Int, resultData: Bundle) {
        when (resultCode) {
            DownloadService.STATUS_RUNNING -> showProgress(true)
            DownloadService.STATUS_FINISHED -> preencherLista(resultData)
            DownloadService.STATUS_ERROR -> {
                val error = resultData.getString(Intent.EXTRA_TEXT)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}
