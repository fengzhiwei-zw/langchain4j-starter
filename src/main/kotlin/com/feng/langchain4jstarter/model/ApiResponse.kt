package com.feng.langchain4jstarter.model

class ApiResponse<E> private constructor(
    val code: Int = 0,
    val message: String? = null,
    val data: E? = null,
    val traceId: String? = null
) {

    class Builder<E> {
        private var code: Int = 0
        private var message: String? = null
        private var data: E? = null
        private var traceId: String? = null

        fun code(code: Int) = apply { this.code = code }
        fun message(message: String?) = apply { this.message = message }
        fun data(data: E?) = apply { this.data = data }
        fun traceId(traceId: String?) = apply { this.traceId = traceId }

        fun build() = ApiResponse(code, message, data, traceId)
    }

    companion object {
        fun <E> builder(): Builder<E> = Builder()

        fun <E> success(data: E?): ApiResponse<E> {
            return builder<E>()
                .code(0)
                .message("success")
                .data(data)
                .build()
        }

        fun <E> failure(code: Int, message: String?, data: E? = null): ApiResponse<E> {
            return builder<E>()
                .code(code)
                .message(message)
                .data(data)
                .build()
        }
    }
}