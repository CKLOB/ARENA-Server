package team.cklob.arena.global.security

import java.security.MessageDigest
import java.util.HexFormat

object RefreshTokenHasher {
    fun hash(token: String): String = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.toByteArray()))
}
