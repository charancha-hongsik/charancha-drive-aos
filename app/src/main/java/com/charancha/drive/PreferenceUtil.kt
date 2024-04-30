package com.charancha.drive

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build


object PreferenceUtil {
    var pref_name = "charancha_pref" //저장소명
    var HAVE_BEEN_HOME = "have_been_home"
    var RUNNING_LEVEL = "running_level"



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

