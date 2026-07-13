package team.cklob.arena.global.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    val status: HttpStatus
    val message: String
    val code: String
        get() = (this as Enum<*>).name
}
