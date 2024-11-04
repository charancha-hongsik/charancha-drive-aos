package com.milelog.retrofit.response

data class VWorldResponse(
    val response: Response
) {
    data class Response(
        val service: Service,
        val status: String,
        val input: Input,
        val result: List<Result>
    ) {
        data class Service(
            val name: String,
            val version: String,
            val operation: String,
            val time: String
        )

        data class Input(
            val point: Point,
            val crs: String,
            val type: String
        ) {
            data class Point(
                val x: String,
                val y: String
            )
        }

        data class Result(
            val zipcode: String,
            val type: String,
            val text: String,
            val structure: Structure
        ) {
            data class Structure(
                val level0: String,
                val level1: String,
                val level2: String,
                val level3: String,
                val level4L: String,
                val level4LC: String,
                val level4A: String,
                val level4AC: String,
                val level5: String,
                val detail: String
            )
        }
    }
}