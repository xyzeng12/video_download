package com.gzk12.videodownload.bean


data class DownloadInfoBean(
        var `data`: Data = Data(),
        var errmsg: String = "",
        var errno: Int = 0
) {
    data class Data(
            var classPeriod: List<ClassPeriod> = listOf(),
            var collect: Boolean = false,
            var course: Course = Course(),
            var isBuy: Boolean = false
    ) {
        data class ClassPeriod(
                var clickNumber: Any = Any(),
                var courseId: Int = 0,
                var courseName: String = "",
                var createTime: String = "",
                var endTime: Any = Any(),
                var gradeId: Int = 0,
                var id: Int = 0,
                var imgUrl: String = "",
                var liveEncodeUrl: Any = Any(),
                var liveStatus: Int = 0,
                var liveUrl: Any = Any(),
                var name: String = "",
                var periodId: Int = 0,
                var price: Int = 0,
                var sortIndex: Int = 0,
                var startTime: Any = Any(),
                var subjectsId: Int = 0,
                var type: Int = 0,
                var updateTime: String = "",
                var videoStatus: Int = 0,
                var videoUrl: String = "",
                var viewsNumber: Any = Any()
        )

        data class Course(
                var classPeriodNum: Int = 0,
                var content: String = "",
                var createTime: String = "",
                var gradeId: Int = 0,
                var id: Int = 0,
                var imgUrl: String = "",
                var joinNum: Int = 0,
                var masterHand: Int = 0,
                var name: String = "",
                var periodId: Int = 0,
                var price: Double = 0.0,
                var professionalTitle: String = "",
                var show: Int = 0,
                var sortIndex: Int = 0,
                var subjectsId: Int = 0,
                var teacherName: String = "",
                var updateTime: String = ""
        )
    }
}