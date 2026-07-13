package team.cklob.arena.global.exception

class ExpectedException(
    val errorCode: ErrorCode,
    messageOverride: String? = null,
) : RuntimeException(messageOverride ?: errorCode.message)
