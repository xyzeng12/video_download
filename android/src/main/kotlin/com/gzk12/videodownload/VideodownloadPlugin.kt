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

    var dataJsonStr = """{
    "errno": 0,
    "data": {
        "isBuy": false,
        "course": {
            "id": 1,
            "periodId": 200,
            "gradeId": 204,
            "subjectsId": 6,
            "sortIndex": 1,
            "show": 1,
            "teacherName": "教师名称",
            "professionalTitle": "一级教师",
            "name": "小学4语文2",
            "imgUrl": "https://k12test.oss-cn-shenzhen.aliyuncs.com/k12-admin-project/school/null/2020/04/16/a4947f141.jpg",
            "content": "<p>大大的</p>",
            "masterHand": 0,
            "classPeriodNum": 500,
            "joinNum": 600,
            "price": 1.01,
            "updateTime": "2020-04-17 18:14:32",
            "createTime": "2020-04-16 11:53:48"
        },
        "collect": false,
        "classPeriod": [
            {
                "id": 1,
                "courseId": 1,
                "periodId": 200,
                "gradeId": 201,
                "subjectsId": 5,
                "sortIndex": 1,
                "name": "课时2",
                "type": 0,
                "videoUrl": "http://1253131631.vod2.myqcloud.com/26f327f9vodgzp1253131631/f4bdff799031868222924043041/playlist.m3u8",
                "liveUrl": null,
                "liveEncodeUrl": null,
                "viewsNumber": null,
                "clickNumber": null,
                "videoStatus": 1,
                "liveStatus": 0,
                "startTime": null,
                "endTime": null,
                "updateTime": "2020-04-17 17:43:56",
                "createTime": "2020-04-17 10:37:31",
                "courseName": "课程1",
                "imgUrl": "https:\/\/k12test.oss-cn-shenzhen.aliyuncs.com\/k12-admin-project\/school\/null\/2020\/04\/16\/a4947f141.jpg",
                "price": 0
            },
            {
                "id": 3,
                "courseId": 1,
                "periodId": 200,
                "gradeId": 201,
                "subjectsId": 5,
                "sortIndex": 1,
                "name": "课时4课时4课时4课时4课时4课时4课时4课时4",
                "type": 0,
                "videoUrl": "http://1252463788.vod2.myqcloud.com/95576ef5vodtransgzp1252463788/68e3febf4564972819220421305/v.f220.m3u8",
                "liveUrl": null,
                "liveEncodeUrl": null,
                "viewsNumber": null,
                "clickNumber": null,
                "videoStatus": 1,
                "liveStatus": 0,
                "startTime": null,
                "endTime": null,
                "updateTime": "2020-04-17 17:43:48",
                "createTime": "2020-04-17 10:37:42",
                "courseName": "课程1",
                "imgUrl": "https:\/\/k12test.oss-cn-shenzhen.aliyuncs.com\/k12-admin-project\/school\/null\/2020\/04\/16\/a4947f141.jpg",
                "price": 0
            }
        ]
    },
    "errmsg": "成功"
}""";
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
        log("当前总任务：$list")

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
                log("http://video.gzk12.com/05698f144c4545778d8e3468b88c8a64/$tsUrl")
                temp.add("http://video.gzk12.com/05698f144c4545778d8e3468b88c8a64/$tsUrl")
            }
            return temp
        }
    }


    @Download.onWait
    fun taskWait(task: DownloadTask) {
        log("taskWait ==> ${task.downloadEntity.fileName}")
    }

    @Download.onTaskStart
    fun taskStart(task: DownloadTask) {
        log("taskStart ==> ${task.downloadEntity.fileName}")
//        mData.find { task.key == it.key }?.btState = false
        notifyDataChanged()
    }

    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        log("taskResume ==> ${task.downloadEntity.fileName}")
//        mData.find { task.key == it.key }?.btState = false
        notifyDataChanged()
    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        log("taskStop ==> ${task.downloadEntity.fileName}")
//        mData.find { task.key == it.key }?.btState = true
        notifyDataChanged()
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        log("taskCancel ==> ${task.downloadEntity.fileName}")
//        mData.find { task.key == it.key }?.btState = true
        notifyDataChanged(task.downloadEntity.url)
    }

    @Download.onTaskFail
    fun taskFail(task: DownloadTask?) {
        log("taskFail ==> ${task?.downloadEntity?.fileName}")
        if (task == null || task.entity == null) {
            return
        }
//        mData.find { task.key == it.key }?.btState = true
        notifyDataChanged()
    }

    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        log("taskComplete ==> ${task.downloadEntity.fileName}")
//        log(FileUtil.getFileMD5(File(task.filePath)))
        log("taskComplete:${task.filePath}")
        notifyDataChanged()
    }

    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        notifyDataChanged()
    }

    private fun notifyDataChanged(deleteUrl: String? = null) {
        val temps = Aria.download(this).totalTaskList
        val maps = HashMap<String, AbsEntity?>()
        temps.forEach {
            it as DownloadEntity
            if (deleteUrl == it.url) {
                maps[it.url] = null
            } else {
                maps[it.url] = it
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