package team.cklob.arena.common

class ExpectedException(
    val errorCode: ErrorCode,
    messageOverride: String? = null,
) : RuntimeException(messageOverride ?: errorCode.message)
