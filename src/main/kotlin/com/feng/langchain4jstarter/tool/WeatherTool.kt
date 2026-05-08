package com.feng.langchain4jstarter.tool

import dev.langchain4j.agent.tool.Tool
import java.time.LocalDateTime


class WeatherTool {
    @Tool("获取指定城市的当前天气信息")
    fun getCurrentWeather(city: String): String {
        // 这里实际应调用天气API，此处用模拟数据演示
        println("【Tool Called】查询天气，城市: $city")

        return when (city.trim { it <= ' ' }) {
            "北京" -> "北京 当前天气：晴朗，温度 18°C，湿度 45%"
            "上海" -> "上海 当前天气：多云，温度 20°C，湿度 60%"
            "广州" -> "广州 当前天气：小雨，温度 24°C，湿度 80%"
            else -> "$city 当前天气：晴，温度 22°C（模拟数据）"
        }
    }

    @get:Tool("获取当前系统时间")
    val currentTime: String
        get() = "当前系统时间：" + LocalDateTime.now()
}