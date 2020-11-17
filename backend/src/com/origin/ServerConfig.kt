package com.origin

import com.typesafe.config.ConfigFactory
import java.io.File

object ServerConfig {
    private const val WORK_DIR = "./"

    @JvmField
    var DB_USER: String? = null

    @JvmField
    var DB_PASSWORD: String? = null

    @JvmField
    var DB_NAME: String? = null

    @JvmStatic
    fun loadConfig() {
        // ищем конфиг в папке с конфигом
        var configFile = File(WORK_DIR + "config/server.conf")
        // если его там нет ищем в корне приложения
        if (!configFile.exists()) {
            configFile = File(WORK_DIR + "server.conf")
        }
        val conf = ConfigFactory
            .parseFile(configFile)
            .withFallback(ConfigFactory.load("server.defaults.conf"))

        DB_USER = conf.getString("db.user")
        DB_PASSWORD = conf.getString("db.password")
        DB_NAME = conf.getString("db.name")
    }
}