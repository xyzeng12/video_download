package com.gzk12.videodownload

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.AbsEntity
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.processor.IVodTsUrlConverter
import com.arialyy.aria.core.task.DownloadTask
import com.google.gson.Gson
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*


/** VideodownloadPlugin */
public class VideodownloadPlugin(private val registrar: Registrar, channel: MethodChannel) : MethodCallHandler {
    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {
        private var channel: MethodChannel? = null

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            Aria.init(registrar.context().applicationContext)
            channel = MethodChannel(registrar.messenger(), "videodownload")
            val videodownloadPlugin = VideodownloadPlugin(registrar, channel!!)
            channel?.setMethodCallHandler(videodownloadPlugin)
            storagePermissions(registrar)
        }

        private fun storagePermissions(registrar: Registrar) {
            val needRequest = ArrayList<String>()
            //SD卡写入
            if (ContextCompat.checkSelfPermission(
                            registrar.context(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                needRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            //sd卡读取
            if (ContextCompat.checkSelfPermission(
                            registrar.context(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    needRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            val strRequest = arrayOfNulls<String>(needRequest.size)
            for (i in needRequest.indices) {
                strRequest[i] = needRequest[i]
            }
            if (strRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(registrar.activity(), strRequest, 1)
            }
        }
    }

    var register = false
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (!register) {
            Aria.download(this).register()
            register = true
        }

        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "download" -> {//开始下载
                download(call, result)
            }
            "pauseDownload" -> {//暂停下载
                pauseDownload(call, result)
            }
            "resumeDownload" -> {//恢复下载
                resumeDownload(call, result)
            }
            "cancelDownload" -> {//取消下载
                cancelDownload(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }


    private fun download(call: MethodCall, result: Result) {
        val map = call.arguments<HashMap<String, Any>>()
        val videoUrl = map["videoUrl"] as String

        val list = Aria.download(this).totalTaskList
        list.find { it.key == videoUrl }.also {
            if (it != null) {
                Aria.download(this).load(it.id).resume(true)
            } else {//找不到说明是新的
                val downloadPath = map["downloadPath"] as String//保存的路径
                if (videoUrl.endsWith("m3u8")) {
                    val option = M3U8VodOption()
                    option.setVodTsUrlConvert(IVodTs())
                    option.generateIndexFile()
                    Aria.download(this)
                            .load(videoUrl)
                            .setFilePath(downloadPath, true)
                            .m3u8VodOption(option)
                            .create()
                } else {
                    Aria.download(this)
                            .load(videoUrl)
                            .setFilePath(downloadPath)
                            .create()
                }
            }
        }
    }

    private fun pauseDownload(call: MethodCall, result: Result) {
        val map = call.arguments<HashMap<String, Any>>()
        val downloadUrl = map["videoUrl"] as String
        val list = Aria.download(this).totalTaskList
        list.find { it.key == downloadUrl }.also {
            if (it != null) {
                Aria.download(this).load(it.id).stop()
            }
        }
    }

    private fun resumeDownload(call: MethodCall, result: Result) {
        val map = call.arguments<HashMap<String, Any>>()
        val downloadUrl = map["videoUrl"] as String
        val list = Aria.download(this).totalTaskList
        list.find { it.key == downloadUrl }.also {
            if (it != null) {
                Aria.download(this).load(it.id).resume()
            }
        }
    }

    private fun cancelDownload(call: MethodCall, result: Result) {
        val map = call.arguments<HashMap<String, Any>>()
        val downloadUrl = map["videoUrl"] as String
        if (downloadUrl == "all") {
            val list = Aria.download(this).totalTaskList
            for (entity in list) {
                Aria.download(this).load(entity.id).cancel(true)
            }
        } else {
            val list = Aria.download(this).totalTaskList
            list.find { it.key == downloadUrl }.also {
                if (it != null) {
                    Aria.download(this).load(it.id).cancel(true)
                }
            }
        }
    }

    class IVodTs : IVodTsUrlConverter {
        override fun convert(
                m3u8Url: String,
                tsUrls: List<String>
        ): List<String> {
            val temp: MutableList<String> =
                    ArrayList()
            for (tsUrl in tsUrls) {
                temp.add("http://video.gzk12.com/05698f144c4545778d8e3468b88c8a64/$tsUrl")
            }
            return temp
        }
    }


    @Download.onWait
    fun taskWait(task: DownloadTask) {
        notifyDataChanged(currentEntity = task.entity)
    }

    @Download.onTaskStart
    fun taskStart(task: DownloadTask) {
        notifyDataChanged(currentEntity = task.entity)
    }

    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        notifyDataChanged(currentEntity = task.entity)
    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        notifyDataChanged(currentEntity = task.entity)
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        notifyDataChanged(task.downloadEntity.url)
    }

    @Download.onTaskFail
    fun taskFail(task: DownloadTask?) {
        if (task == null || task.entity == null) {
            return
        }
        notifyDataChanged(currentEntity = task.entity)
    }

    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        notifyDataChanged(currentEntity = task.entity)
    }

    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        notifyDataChanged(currentEntity = task.entity)
    }

    var temps: MutableList<AbsEntity>? = null
    private fun notifyDataChanged(deleteUrl: String? = null, currentEntity: AbsEntity? = null) {
        val newTaskList = Aria.download(this).totalTaskList
        if (temps == null || temps!!.isEmpty() || (temps!!.size != newTaskList.size)) {
            temps = newTaskList
        }
        val maps = HashMap<String, AbsEntity?>()
        temps?.forEachIndexed { index, absEntity ->
            val saveEntity = if (currentEntity != null &&
                    currentEntity.key == absEntity.key
            ) {
                currentEntity
            } else {
                absEntity
            }
            temps!![index] = saveEntity
            saveEntity as DownloadEntity
            if (deleteUrl == saveEntity.url) {
                maps[saveEntity.url] = null
            } else {
                maps[saveEntity.url] = saveEntity
            }
        }
        val tempsStr = Gson().toJson(maps)
        channel?.invokeMethod("downloadListener", tempsStr)
    }
}

fun log(text: Any?) {
    Log.i("xyzeng", "------------------>>>------------------\n")
    Log.i("xyzeng", "$text")
    Log.i("xyzeng", "------------------<<<------------------\n")
}