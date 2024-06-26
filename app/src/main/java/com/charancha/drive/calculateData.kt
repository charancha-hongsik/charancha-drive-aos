package com.charancha.drive


import com.charancha.drive.room.dto.EachGpsDto
import java.text.SimpleDateFormat
import java.util.*


/**
 *
 *      1. 기본 전제 데이터를 계산하기 위한 함수 모음
 *
 *     @field:PrimaryKey var tracking_id: String, // 20240417190026
 *      ->
 *     var timeStamp: Long, // 1714087621
 *      -> 시작 시간
 *
 *     var verification: String, // L1, L2, L3, L4
 *      ->
 *
 *     var distance: Float, // 12533.736
 *      -> 총 거리
 *
 *     var time: Long, // 2015.614
 *      -> 총 시간
 *
 *     var jsonData: String // 원시 데이터
 *      ->
 *
 *
 *      2. Room DB에 들어가있는 기본 전제 데이터들로 지표 데이터 구하는 함수 모음
 */
object calculateData {
    val MS_TO_KH = 3.6f

    private fun getDateFromTimeStamp(timeStamp:Long) : Int{
        val format = SimpleDateFormat("HH")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }

    /**
     * distance
     */
    fun getDistance(gpsInfo:MutableList<EachGpsDto>):List<Float>{
        try {
            val list = MutableList(23) { 0f }
            for (info in gpsInfo) {
                list[getDateFromTimeStamp(info.timeStamp)] = list[getDateFromTimeStamp(info.timeStamp)] + info.distance
            }


            return list.toList()

        }catch (e:Exception){
            return List(23){0f}
        }
    }

    /**
     *  var sudden_deceleration: Int, // 0,1,2
     *  -> 급감속 (count)
     *  -> 초당 14km/h이상 감속 운행하고 속도가 6.0km/h 이상인 경우
     */
    fun getSuddenDeceleration(gpsInfo:MutableList<EachGpsDto>):List<Int>{
        try {
            val list = MutableList(23) { 0 }
            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH >= 6f && info.acceleration * MS_TO_KH <= -14f) {
                    list[getDateFromTimeStamp(info.timeStamp)]++
                }
            }


            return list.toList()

        }catch (e:Exception){
            return List(23){0}
        }
    }


    /**
     *  var sudden_stop: Int, // 0,1,2
     *  -> 급정지 (count)
     *  -> 초당 14km/h이상 감속 운행하고 속도가 5.0km/h 이하인 경우
     */
    fun getSuddenStop(gpsInfo:MutableList<EachGpsDto>):List<Int>{
        try {
            val list = MutableList(23) { 0 }

            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH >= 5f && info.acceleration * MS_TO_KH <= -14f) {
                    list[getDateFromTimeStamp(info.timeStamp)]++
                }
            }

            return list.toList()
        } catch (e:Exception){
            return List(23){0}
        }
    }

    /**
     *  var sudden_acceleration: Int, // 0,1,2
     *  -> 급가속 (count)
     *  -> 10km/h 초과 속도에서 초당 10km/h 이상 가속 운행한 경우
     */
    fun getSuddenAcceleration(gpsInfo:MutableList<EachGpsDto>):List<Int>{
        try {
            val list = MutableList(23) { 0 }

            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH >= 10f && info.acceleration * MS_TO_KH >= 10f) {
                    list[getDateFromTimeStamp(info.timeStamp)]++
                }
            }

            return list.toList()
        }catch (e:Exception){
            return List(23){0}
        }
    }

    /**
     * var sudden_start: Int, // 0,1,2
     *  -> 급출발 (count)
     *  -> 5.0km/h 이하 속도에서 출발하여 초당 10km/h이상 가속 운행한 경우
     */
    fun getSuddenStart(gpsInfo:MutableList<EachGpsDto>):List<Int>{
        try {
            val list = MutableList(23) { 0 }

            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH <= 5f && info.acceleration * MS_TO_KH >= 10f) {
                    list[getDateFromTimeStamp(info.timeStamp)]++
                }
            }

            return list.toList()
        }catch (e:Exception){
            return List(23){0}
        }
    }

    /**
     *  var high_speed_driving: Float, // 12533.736
     *  -> 고속 주행 거리 (distance)
     *  -> 80km/h 이상 ~ 150km/h 이하 속력으로 주행한 거리의 총합
     */
    fun getHighSpeedDriving(gpsInfo:MutableList<EachGpsDto>):List<Float>{
        try {
            val list = MutableList(23) { 0f }

            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH in 80f..150f) {
                    list[getDateFromTimeStamp(info.timeStamp)] = list[getDateFromTimeStamp(info.timeStamp)] + info.distance
                }
            }

            return list.toList()
        }catch (e:Exception){
            return List(23){0f}
        }
    }

    /**
     *  var low_speed_driving: Float, // 12533.736
     *  -> 저속 주행 거리 (distance)
     *  -> 40km/h 미만 속력으로 주행한 거리의 총합
     */
    fun getLowSpeedDriving(gpsInfo:MutableList<EachGpsDto>):List<Float>{
        try {
            val list = MutableList(23) { 0f }

            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH in 0f..39.9999f) {
                    list[getDateFromTimeStamp(info.timeStamp)] = list[getDateFromTimeStamp(info.timeStamp)] + info.distance
                }
            }

            return list.toList()
        }catch (e:Exception){
            return List(23){0f}
        }
    }


    /**
     * 항속 주행거리
     * 주행 시작 시 3분마다 체크한다.
     * 속도 61이상 ~ 141미만
     * 속도 범위 중 1개가 80%이상 비중을 차지한다.
     *
     * 속도 범위
     * 61 이상 ~ 81 미만
     * 81 이상 ~ 101 미만
     * 101 이상 ~ 121 미만
     * 121 이상 ~ 141 미만
     */
    fun getConstantSpeedDriving(gpsInfo:MutableList<EachGpsDto>):List<Float>{
        try {
            val list = MutableList(24) { 0f }
            var constantList1 = MutableList(24) {0f}
            var constantList2 = MutableList(24) {0f}
            var constantList3 = MutableList(24) {0f}
            var constantList4 = MutableList(24) {0f}
            var constantList5 = MutableList(24) {0f}
            var firstConstantTimeStamp = 0L

            for (info in gpsInfo) {

                if(firstConstantTimeStamp == 0L){
                    // 첫 3분 시작 시점
                    firstConstantTimeStamp = info.timeStamp
                    if (info.speed * MS_TO_KH in 61f..80f) {
                        constantList1[getDateFromTimeStamp(info.timeStamp)] = constantList1[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if(info.speed * MS_TO_KH in 81f..100f){
                        constantList2[getDateFromTimeStamp(info.timeStamp)] = constantList1[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if(info.speed * MS_TO_KH in 101f..120f){
                        constantList3[getDateFromTimeStamp(info.timeStamp)] = constantList1[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if(info.speed * MS_TO_KH in 121f..140f){
                        constantList4[getDateFromTimeStamp(info.timeStamp)] = constantList1[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else{
                        constantList5[getDateFromTimeStamp(info.timeStamp)] =
                            constantList5[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    }


                } else if(info.timeStamp - firstConstantTimeStamp >= 180000){
                    var countSum = constantList1.sum() + constantList2.sum() + constantList3.sum() + constantList4.sum() + constantList5.sum()
                    // n번째 3분 시작 시점


                    if(constantList1.sum()/countSum >= 0.8f || constantList2.sum()/countSum >= 0.8f || constantList3.sum()/countSum >= 0.8f || constantList4.sum()/countSum >= 0.8f){
                        for(i: Int in 0..23){
                            list[i] = list[i] + constantList1[i] + constantList2[i] + constantList3[i] + constantList4[i]
                        }
                    }

                    firstConstantTimeStamp = info.timeStamp
                    constantList1 = MutableList(24) {0f}
                    constantList2 = MutableList(24) {0f}
                    constantList3 = MutableList(24) {0f}
                    constantList4 = MutableList(24) {0f}
                    constantList5 = MutableList(24) {0f}

                    // 3분 간격 동안 계속 쌓기
                    if (info.speed * MS_TO_KH in 61f..80f) {
                        constantList1[getDateFromTimeStamp(info.timeStamp)] =
                            constantList1[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if (info.speed * MS_TO_KH in 81f..100f) {
                        constantList2[getDateFromTimeStamp(info.timeStamp)] =
                            constantList3[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if (info.speed * MS_TO_KH in 101f..120f) {
                        constantList3[getDateFromTimeStamp(info.timeStamp)] =
                            constantList4[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if (info.speed * MS_TO_KH in 121f..140f) {
                        constantList4[getDateFromTimeStamp(info.timeStamp)] =
                            constantList4[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else{
                        constantList5[getDateFromTimeStamp(info.timeStamp)] =
                            constantList5[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    }


                } else {
                    // 3분 간격 동안 계속 쌓기
                    if (info.speed * MS_TO_KH in 61f..80f) {
                        constantList1[getDateFromTimeStamp(info.timeStamp)] =
                            constantList1[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if (info.speed * MS_TO_KH in 81f..100f) {
                        constantList2[getDateFromTimeStamp(info.timeStamp)] =
                            constantList3[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if (info.speed * MS_TO_KH in 101f..120f) {
                        constantList3[getDateFromTimeStamp(info.timeStamp)] =
                            constantList4[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else if (info.speed * MS_TO_KH in 121f..140f) {
                        constantList4[getDateFromTimeStamp(info.timeStamp)] =
                            constantList4[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    } else{
                        constantList5[getDateFromTimeStamp(info.timeStamp)] =
                            constantList5[getDateFromTimeStamp(info.timeStamp)] + info.distance
                    }
                }


            }

            return list.toList()
        } catch (e:Exception){
            return List(23){0f}
        }
    }

    /**
     *  var harsh_driving:Float, // 12533.736
     *  -> 가혹 주행 거리 (distance)
     */
    fun getHarshDriving(gpsInfo:MutableList<EachGpsDto>):List<Float>{
        try {
            val list = MutableList(23) { 0f }

            var pastSpeed = 0f


            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH >= 6f && info.acceleration * MS_TO_KH <= -14f) {
                    list[getDateFromTimeStamp(info.timeStamp)] = list[getDateFromTimeStamp(info.timeStamp)] + info.distance
                }

                if (info.speed * MS_TO_KH >= 5f && info.acceleration * MS_TO_KH <= -14f) {
                    list[getDateFromTimeStamp(info.timeStamp)] = list[getDateFromTimeStamp(info.timeStamp)] + info.distance
                }

                if (info.speed * MS_TO_KH >= 10f && info.acceleration * MS_TO_KH >= 10f) {
                    list[getDateFromTimeStamp(info.timeStamp)] = list[getDateFromTimeStamp(info.timeStamp)] + info.distance
                }

                if (pastSpeed * MS_TO_KH <= 5f && info.acceleration * MS_TO_KH >= 10f) {
                    list[getDateFromTimeStamp(info.timeStamp)] = list[getDateFromTimeStamp(info.timeStamp)] + info.distance
                }

                pastSpeed = info.speed
            }

            return list.toList()
        } catch (e:Exception){
            return MutableList(23) { 0f }
        }
    }

    /**
     * 급감속 속력의 총 합
     */
    fun getSumSuddenDecelerationDistance(gpsInfo:MutableList<EachGpsDto>):Float{
        try {
            var distanceSum = 0f
            for (info in gpsInfo) {
                if (info.speed * MS_TO_KH >= 6f && info.acceleration * MS_TO_KH <= -14f) {
                    distanceSum += info.distance
                }

                if (info.speed * MS_TO_KH >= 10f && info.acceleration * MS_TO_KH >= 10f) {
                    distanceSum += info.distance
                }
            }


            return distanceSum

        }catch (e:Exception){
            return 0f
        }
    }






    /**
     * 평균 주행 거리
     * n일간 총 주행거리 / n일간 총 주행 일수
     */
    fun getAverageDistance(driveDistance:Float, driveDay:Int): Float{
        return driveDistance/driveDay
    }

    /**
     * 1일 최대 거리
     * n일 중 주행 거리 max값
     */
    fun getMaximumDistancePerDay(): Float{
        return 0f
    }

    /**
     * 1일 최소 거리
     * n일 중 주행 거리 minimum값 (0제외)
     */
    fun getMinimumDistancePerDay(): Float{
        return 0f
    }

    /**
     * 누적 주행 거리
     * n일간 누적 주행 거리
     */
    fun getTotalDistance():String{
        return ""
    }

    /**
     * 평균 주행 시간
     * n일간 총 주행 시간 / n일간 총 주행 일수
     */
    fun getAverageDrivingTime():String{
        return ""
    }

    /**
     * 1일 최대 주행 시간
     * n일 중 주행 시간 max값
     */
    fun getMaximumDrivingTimePerDay():String{
        return ""
    }

    /**
     * 1일 최소 주행 시간
     * n일 중 주행 시간 minimum값 (0제외)
     */
    fun getMinimumDrivingTimePerDay():String{
        return ""
    }

    /**
     * 누적 주행 시간
     * n일간 누적 주행 시간
     */
    fun getTotalDrivingTime():String{
        return ""
    }

    /**
     * n일 1회 평균 주행 거리
     * n일 동안 (총 주행 거리 / 총 주행 횟수)
     */
    fun getAverageDistancePerTrip():Float{
        return 0f
    }

    /**
     * 1회 최대거리
     * n일 중 1회 평균 주행 거리 max값
     */
    fun getMaximumDistancePerTrip():Float{
        return 0f
    }

    /**
     * 1일 최소 거리
     * n일 중 1회 평균 주행 거리 minimum값 (0제외)
     */
    fun getMinimumDistancePerTrip():Float{
        return 0f
    }

    /**
     * 평균 고속 주행
     * [(총 고속 주행 거리 / 총 주행 일수)/(저속 주행 거리+고속 주행 거리)]*100
     */
    fun getAverageHighSpeedDrivingRatio():Float{
        return 0f
    }

    /**
     * 고속 주행
     * [고속 주행 거리/(저속 주행 거리+고속 주행 거리)]*100
     */
    fun getHighSpeedDrivingRatio():Float{
        return 0f
    }

    /**
     * 저속 주행
     * [저속 주행 거리/(저속 주행 거리+고속 주행 거리)]*100
     */
    fun getLowSpeedDrivingRatio():Float{
        return 0f
    }

    /**
     * 평균 최적 주행
     * [n일간 총 최적 주행 거리/(n일간 총 가혹 주행 거리 + n일간 총 최적 주행 거리)]*100
     */
    fun getAverageOptimalDrivingRatio():Float{
        return 0f
    }

    /**
     * 최적 주행
     * [최적 주행 거리 /(최적 주행 거리+가혹 주행 거리)]*100
     */
    fun getOptimalDrivingRatio():Float{
        return 0f
    }

    /**
     * 가혹 주행
     * [가혹 주행 거리 /(최적 주행 거리+가혹 주행 거리)]*100
     */
    fun getHarshDrivingRatio():Float{
        return 0f
    }

    /**
     * 평균 항속 주행
     * [n일간 총 항속 주행 거리/(n일간 총 항속 주행 거리 + n일간 총 변속 주행 거리)]*100
     */
    fun getAverageConstantSpeedDrivingRatio():Float{
        return 0f
    }

    /**
     * 항속 주행
     * [항속 주행 거리 /(항속 주행 거리+변속 주행 거리)]*100
     */
    fun getConstantSpeedDrivingRatio():Float{
        return 0f
    }

    /**
     * 변속 주행
     * [변속 주행 거리 /(항속 주행 거리+변속 주행 거리)]*100
     */
    fun getShiftingDrvingRatio():Float{
        return 0f
    }

    /**
     * 평균 종합 횟수
     * n일간 총 종합 횟수/n일
     */
    fun getAverageAggregateCount():Int{
        return 0
    }

    /**
     * 1일 최대 종합 횟수
     * n일 중 종합 횟수 max값
     */
    fun getMaximumAggregateCountPerDay():Int{
        return 0
    }

    /**
     * 1일 최소 종합 횟수
     * n일 중 종합 횟수 minimum값 (0제외)
     */
    fun getMinimumAggregateCountPerDay():Int{
        return 0
    }

    /**
     * 누적 종합 횟수
     * n일간 누적 종합 횟수
     */
    fun getTotalAggregateCount():Int{
        return 0
    }

    /**
     * 평균 급출발
     * n일간 총 급출발/n일
     */
    fun getAverageSuddenStart():Int{
        return 0
    }

    /**
     * 1일 최대 급출발
     * n일 중 급출발 max값
     */
    fun getMaximumSuddenStartPerDay():Int{
        return 0
    }

    /**
     * 1일 최소 급출발
     * n일 중 급출발 minimum값 (0제외)
     */
    fun getMinimumSuddenStartPerDay():Int{
        return 0
    }

    /**
     * 누적 급출발
     * n일간 누적 급출발
     */
    fun getTotalSuddenStart():Int{
        return 0
    }

    /**
     * 평균 급가속
     * n일간 총 급출발/n일
     */
    fun getAverageSuddenAcceleration():Int{
        return 0
    }

    /**
     * 1일 최대 급가속
     * n일 중 급출발 max값
     */
    fun getMaximumSuddenAccelerationPerDay():Int{
        return 0
    }

    /**
     * 1일 최소 급가속
     * n일 중 급출발 minimum값 (0제외)
     */
    fun getMinimumSuddenAccelerationPerDay():Int{
        return 0
    }

    /**
     * 누적 급가속
     * n일간 누적 급출발
     */
    fun getTotalSuddenAcceleration():Int{
        return 0
    }

    /**
     * 평균 급정지
     * n일간 총 급정지/n일
     */
    fun getAverageSuddenStop():Int{
        return 0
    }

    /**
     * 1일 최대 급정지
     * n일 중 급정지 max값
     */
    fun getMaximumSuddenStopPerDay():Int{
        return 0
    }

    /**
     * 1일 최소 급정지
     * n일 중 급정지 minimum값 (0제외)
     */
    fun getMinimumSuddenStopPerDay():Int{
        return 0
    }

    /**
     * 누적 급정지
     * n일간 누적 급정지
     */
    fun getTotalSuddenStop():Int{
        return 0
    }

    /**
     * 평균 급감속
     * n일간 총 급감속/n일
     */
    fun getAverageSuddenDeceleration():Int{
        return 0
    }

    /**
     * 1일 최대 급감속
     * n일 중 급감속 max값
     */
    fun getMaximumSuddenDecelerationPerDay():Int{
        return 0
    }

    /**
     * 1일 최소 급감속
     * n일 중 급감속 minimum값 (0제외)
     */
    fun getMinimmSuddenDecelerationPerDay():Int{
        return 0
    }

    /**
     * 누적 급감속
     * n일간 누적 급감속
     */
    fun getTotalSuddenDeceleration():Int{
        return 0
    }

    /**
     * 평균 급감속 속력
     * n일 중 급감속 속력의 총합/n일 중 총 급감속 횟수
     * e.g.
     * 30일 중 급감속 3회 발생
     * 1회:20km/h, 2회: 15km/h, 3회: 30km/h
     * 평균 급감속 속력 21.67km/h
     */
    fun getAverageSuddenDecelerationSpeed():Float{
        return 0f
    }




}