package com.charancha.drive

import android.util.Log
import com.charancha.drive.room.DriveDto
import com.charancha.drive.room.EachGpsDto
import com.charancha.drive.room.entity.Drive
import com.google.gson.Gson
import java.lang.Math.*
import kotlin.math.pow


/**
 * 1. 기본 전제 데이터를 계산하기 위한 함수 모음
 * 2. Room DB에 들어가있는 기본 전제 데이터들로 지표 데이터 구하는 함수 모음
 */
object calculateData {
    val MS_TO_KH = 3.6f

    /**
     *  var sudden_deceleration: Int, // 0,1,2
     *  -> 급감속 (count)
     *  -> 초당 14km/h이상 감속 운행하고 속도가 6.0km/h 이상인 경우
     *  -> 초당 14km/h ->
     */
    fun getSuddenDeceleration(rawData: String):Int{
        val gpsInfo = Gson().fromJson(rawData, DriveDto::class.java).rawData.toMutableList()

        var count:Int = 0

        for(info in gpsInfo){
            if(info.speed * MS_TO_KH >= 6f && info.acceleration <= -14f){
                Log.d("testsetsetest","testestesteestset speed :: " + info.speed)
                Log.d("testsetsetest","testestesteestset acceleration :: " + info.acceleration)

                count++
            }
        }

        return count
    }

    fun getDistanceFromHaversine(drive: Drive):String{
        val gpsInfo = Gson().fromJson(drive.jsonData, DriveDto::class.java).rawData.toMutableList()
        val title = drive.tracking_id
        if(title == "20240514120935"){
            Log.d("testsetsetset","testsetsetsetset :: 20240514120935")

            var contents:String = ""
            var maximumDistance = 0.0
            val firstLatitude = gpsInfo[10].latitude
            val firstAltitude = gpsInfo[10].longtitude



            for(info in gpsInfo){
                contents = contents + "(" + firstLatitude + " ," + firstAltitude +")" + "(" +info.latitude + " ," + info.longtitude + ")" + " 사이 거리 : " + haversine(firstLatitude, firstAltitude, info.latitude, info.longtitude).toString() + "\n"
                if(haversine(firstLatitude, firstAltitude, info.latitude, info.longtitude) > maximumDistance){
                    maximumDistance = haversine(firstLatitude, firstAltitude, info.latitude, info.longtitude)
                }
            }


            contents = contents + "maxDistance :: " + maximumDistance
            Log.d("testsetsetset","testsetsetsetset :: " + contents)
            return contents
        } else{
            return ""
        }
    }


    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // 지구 반지름 (킬로미터 단위)

        // 위도와 경도를 라디안으로 변환
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // 하버사인 공식 적용
        val dlon = lon2Rad - lon1Rad
        val dlat = lat2Rad - lat1Rad

        val a = sin(dlat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dlon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // 거리 계산 (킬로미터 단위)
        val distanceKm = R * c

        // 거리를 미터 단위로 변환
        return distanceKm * 1000
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