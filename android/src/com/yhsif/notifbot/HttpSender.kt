package com.yhsif.notifbot

import android.os.AsyncTask

import java.io.IOException

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpSender(
  val onSuccess: () -> Unit,
  val onFailure: () -> Unit,
  val onNetFail: () -> Unit
) : AsyncTask<Request, Void, Int>() {

  companion object {
    private const val KEY_LABEL = "label"
    private const val KEY_MSG = "msg"
    private const val CODE_NET_FAIL = -1

    private val client = OkHttpClient.Builder().followRedirects(false).build()

    fun send(
      url: String,
      label: String,
      msg: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit,
      onNetFail: () -> Unit
    ) {
      val body =
        FormBody.Builder().add(KEY_LABEL, label).add(KEY_MSG, msg).build()
      val request = Request.Builder().url(url).post(body).build()

      HttpSender(onSuccess, onFailure, onNetFail).execute(request)
    }

    fun checkUrl(url: String, onFailure: () -> Unit) {
      val request = Request.Builder().url(url).get().build()
      HttpSender({}, onFailure, {}).execute(request)
    }
  }

  override fun doInBackground(vararg reqs: Request): Int {
    for (req in reqs) {
      // Only handle the first req
      try {
        val res = client.newCall(req).execute()
        val code = res.code()
        res.close()
        return code
      } catch (_: IOException) {
        return CODE_NET_FAIL
      }
    }
    // Empty reqs
    return 404
  }

  override fun onPostExecute(code: Int) {
    if (code == CODE_NET_FAIL) {
      return onNetFail()
    }
    if (code >= 200 && code < 400) {
      onSuccess()
    } else {
      onFailure()
    }
  }
}
