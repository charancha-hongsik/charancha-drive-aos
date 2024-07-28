package com.milelog

import android.app.Activity
import android.content.Context


object PreferenceUtil {
    const val pref_name = "charancha_pref" //저장소명
    const val HAVE_BEEN_HOME = "have_been_home"
    const val RUNNING_LEVEL = "running_level"
    const val USER_NAME = "username"
    const val PERMISSION_ACTIVITY_RECOGNITION_THREE_TIMES = "PERMISSION_ACTIVITY_RECOGNITION_THREE_TIMES"
    const val PERMISSION_ACCESS_BACKGROUND_LOCATION_THREE_TIMES = "PERMISSION_ACCESS_BACKGROUND_LOCATION_THREE_TIMES"
    const val PERMISSION_ACCESS_FINE_LOCATION_THREE_TIMES = "PERMISSION_ACCESS_FINE_LOCATION_THREE_TIMES"
    const val PERMISSION_ACCESS_BLUETOOTH_THREE_TIMES = "PERMISSION_ACCESS_FINE_LOCATION_THREE_TIMES"
    const val PERMISSION_ACCESS_ALARM_THREE_TIMES = "PERMISSION_ACCESS_FINE_LOCATION_THREE_TIMES"
    const val PERMISSION_ALL_CHECKED = "PERMISSION_ALL_CHECKED"


    /**
     * 로그인 및 로그아웃 시 처리 필요
     */
    val ACCESS_TOKEN = "access_token"
    val REFRESH_TOKEN = "refresh_token"
    val EXPIRES_IN = "expires_in"
    val REFRESH_EXPIRES_IN = "refresh_expires_in"
    val TOKEN_TYPE = "token_type"
    val KEYLESS_ACCOUNT = "keylessAccount"
    val KEYLESS_ACCOUNT_EXPIRE = "keylessAccountExpire"
    val OAUTH_PROVIDER = "oauthProvider"
    val ID_TOKEN = "idToken"
    val ACCOUNT_ADDRESS = "accountAddress"
    val USER_CARID = "user_carid"


    /**
     * km/mile
     */
    val KM_MILE = "km_mile"




    /**
     *
     * key 값에 해당하는  String 타입을 받음
     * @param context    app context
     * @param key            다른 값들과 구분할 유일한 key 값
     * @param defaultVal    값이 없을 경우 기본값
     * @return String 타입 값
     */
    fun getPref(context: Context, key: String?, defaultVal: String?): String? {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        return pref.getString(key, defaultVal)
    }

    /**
     *
     * key 값에 해당하는  Boolean 타입을 받음
     * @param context    app context
     * @param key            다른 값들과 구분할 유일한 key 값
     * @param defaultVal    값이 없을 경우 기본값
     * @return Boolean 타입 값
     */
    fun getBooleanPref(context: Context, key: String?, defaultVal: Boolean): Boolean {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        return pref.getBoolean(key, defaultVal)
    }

    /**
     *
     * key 값에 해당하는  Long 타입을 받음
     * @param context    app context
     * @param key            다른 값들과 구분할 유일한 key 값
     * @param defaultVal    값이 없을 경우 기본값
     * @return Long 타입 값
     */
    fun getLongPref(context: Context, key: String?, defaultVal: Long?): Long {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        return pref.getLong(key, defaultVal!!)
    }

    /**
     *
     * key 값에 해당하는  Int 타입을 받음
     * @param context    app context
     * @param key            다른 값들과 구분할 유일한 key 값
     * @param defaultVal    값이 없을 경우 기본값
     * @return Int 타입 값
     */
    fun getIntPref(context: Context, key: String?, defaultVal: Int): Int {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        return pref.getInt(key, defaultVal)
    }

    /**
     *
     * 키 저장
     * @param context Context
     * @param key     key값
     * @param value   저장할 String 값
     */
    fun putPref(context: Context, key: String?, value: String?) {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.commit()
    }

    /**
     *
     * 키 저장
     * @param context Context
     * @param key     key값
     * @param value   저장할 boolean 값
     */
    fun putBooleanPref(context: Context, key: String?, value: Boolean) {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    /**
     *
     * 키 저장
     * @param context Context
     * @param key     key값
     * @param value   저장할 long 값
     */
    fun putLongPref(context: Context, key: String?, value: Long) {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.commit()
    }

    /**
     *
     * 키 저장
     * @param context Context
     * @param key     key값
     * @param value   저장할 int 값
     */
    fun putIntPref(context: Context, key: String?, value: Int) {
        val pref = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.commit()
    }
}

