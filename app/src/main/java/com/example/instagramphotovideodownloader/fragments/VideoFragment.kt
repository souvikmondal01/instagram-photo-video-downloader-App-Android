package com.example.instagramphotovideodownloader.fragments

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.instagramphotovideodownloader.MySingleton
import com.example.instagramphotovideodownloader.R
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.android.synthetic.main.fragment_video.view.*
import org.json.JSONObject
import java.io.File

class VideoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        view.btn_video_load.setOnClickListener {
            try {
                if (view.et_video.text.isNotEmpty()) {
                    loadVideo()
                }
            } catch (s: Exception) {
                Toast.makeText(context, "Enter valid Link !!", Toast.LENGTH_SHORT).show()
            }
        }
        view.btn_video_paste_link.setOnClickListener {
            try {
                iv_cancel_video.visibility = View.VISIBLE
                pasteLink()
                loadVideo()
            } catch (e: Exception) {

            }
        }
        view.btn_video_download.setOnClickListener {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    askPermissions()
                } else {
                    if (view.et_video.text.isNotEmpty())
                        loadVideo()
                    downloadVideo()
                }
            } catch (s: Exception) {
                Toast.makeText(context, "Enter valid Link !!", Toast.LENGTH_SHORT).show()
            }
        }
        view.iv_cancel_video.setOnClickListener {
            et_video.text = null
            iv_cancel_video.visibility = View.INVISIBLE
        }

        return view
    }

    private fun loadVideo() {
        vv_video.visibility = View.VISIBLE
        val imageUrl = et_video.text
        val abc = imageUrl.substring(0, imageUrl.indexOf("?")) + "?__a=1"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, abc, null,
            { response ->
                val graphqlObject: JSONObject = response.getJSONObject("graphql")
                val shortcodeMediaObject: JSONObject =
                    graphqlObject.getJSONObject("shortcode_media")
                val nameValue = shortcodeMediaObject.getString("video_url")

                val mediaController = MediaController(context)
                mediaController.setAnchorView(vv_video)

                val onlineUri: Uri = Uri.parse(nameValue)

                vv_video.setMediaController(mediaController)
                vv_video.setVideoURI(onlineUri)
                vv_video.requestFocus()
                vv_video.start()

            },
            {
            })
        context?.let { MySingleton.getInstance(it).addToRequestQueue(jsonObjectRequest) }
    }

    private fun pasteLink() {
        val clipboard: ClipboardManager? =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        if (clipboard?.hasPrimaryClip() == true) {
            et_video.setText(clipboard.primaryClip?.getItemAt(0)?.text.toString())
        }
    }

    private fun downloadVideo() {
        val fileName = "IG_video_${System.currentTimeMillis()}"
        val imageUrl = et_video.text
        val abc = imageUrl.substring(0, imageUrl.indexOf("?")) + "?__a=1"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, abc, null,
            { response ->
                val graphqlObject: JSONObject = response.getJSONObject("graphql")
                val shortcodeMediaObject: JSONObject =
                    graphqlObject.getJSONObject("shortcode_media")
                val nameValue = shortcodeMediaObject.getString("video_url")

                try {
                    val downloadManager: DownloadManager?
                    downloadManager =
                        context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val downloadUri = Uri.parse(nameValue)
                    val request = DownloadManager.Request(downloadUri).setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                    ).setAllowedOverRoaming(false).setTitle(fileName).setMimeType("video/mp4")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_PICTURES,
                            File.separator + fileName + ".mp4"
                        )

                    downloadManager.enqueue(request)
                    Toast.makeText(context, "Download Done", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Toast.makeText(context, "Download Fail", Toast.LENGTH_SHORT).show()
                }

            },
            {
            })
        context?.let { MySingleton.getInstance(it).addToRequestQueue(jsonObjectRequest) }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askPermissions() {

        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                AlertDialog.Builder(context)
                    .setTitle("Permission required")
                    .setMessage("Permission required to save photos from the Web.")
                    .setPositiveButton("Allow") { _, _ ->
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                        (context as Activity).finish()
                    }
                    .setNegativeButton("Deny") { dialog, _ -> dialog.cancel() }
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                )
            }
        } else {
            downloadVideo()
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }


}