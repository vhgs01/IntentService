package br.com.hugo.victor.intentservice

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.text.TextUtils
import android.webkit.WebChromeClient.FileChooserParams.parseResult
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService(DownloadService::class.java.name) {

    companion object {
        val STATUS_RUNNING = 0
        val STATUS_FINISHED = 1
        val STATUS_ERROR = 2
    }

    override fun onHandleIntent(intent: Intent?) {
        val receiver = intent!!.getParcelableExtra<ResultReceiver>("receiver")
        val url = intent.getStringExtra("url")
        val bundle = Bundle()

        if (!TextUtils.isEmpty(url)) {
            // ATUALIZA A UI: DOWNLOAD SERVICE RODANDO
            receiver.send(STATUS_RUNNING, Bundle.EMPTY)
            try {
                val results = downloadData(url)
                // ENVIA O REUSLTADO DE VOLTA PARA A ACTIVITY
                if (null != results && results.isNotEmpty()) {
                    bundle.putStringArray("result", results.toTypedArray())
                    receiver.send(STATUS_FINISHED, bundle)
                }
            } catch (e: Exception) {
                // ENVIA MENSAGEM DE ERRO PARA A ACTIVITY
                bundle.putString(Intent.EXTRA_TEXT, e.toString())
                receiver.send(STATUS_ERROR, bundle)
            }
        }

        this.stopSelf()
    }

    @Throws(IOException::class, DownloadException::class)
    private fun downloadData(requestUrl: String): List<String?> {
        var inputStream: InputStream?
        var urlConnection: HttpURLConnection?
        val url = URL(requestUrl)

        urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("Accept", "application/json")
        urlConnection.requestMethod = "GET"

        val statusCode = urlConnection.responseCode

        if (statusCode == 200) {
            inputStream = BufferedInputStream(urlConnection.inputStream)
            val response = convertInputStreamToString(inputStream)
            val result = parseResult(response)
            return result.toList()
        } else {
            throw DownloadException("Falha na busca de dados")
        }
    }

    @Throws(IOException::class)
    private fun convertInputStreamToString(inputStream: InputStream?): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream!!))
        var line = bufferedReader.readLine()
        var result = ""

        while (line != null) {
            result += line
            line = bufferedReader.readLine()
        }

        inputStream?.close()

        return result
    }

    private fun parseResult(result: String) : List<String?>{
        var nomePokemons: Array<String?> = arrayOf()
        try {
            val response = JSONObject(result)
            val pokemons = response.optJSONArray("results")
            nomePokemons = arrayOfNulls(pokemons.length())

            for (i in 0 until pokemons.length()) {
                val pokemon = pokemons.optJSONObject(i)
                val nomePokemon = pokemon.optString("name")
                nomePokemons[i] = nomePokemon
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return nomePokemons.toList()
    }
}