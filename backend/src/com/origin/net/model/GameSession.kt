package com.origin.net.model

import com.origin.ServerConfig
import com.origin.entity.Account
import com.origin.entity.Character
import com.origin.entity.Characters
import com.origin.model.Player
import com.origin.net.GameServer
import com.origin.net.api.AuthorizationException
import com.origin.net.api.BadRequest
import com.origin.net.gsonSerializer
import com.origin.net.logger
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * игровая сессия (коннект)
 */
@ObsoleteCoroutinesApi
class GameSession(private val connect: DefaultWebSocketSession) {
    var ssid: String? = null
        private set

    private var account: Account? = null

    private var player: Player? = null

    suspend fun received(r: GameRequest) {

        // инициализация сессии
        if (ssid == null) {
            // начальная точка входа клиента в игру (авторизация по ssid)
            // также передается выбранный персонаж
            if (r.target == "ssid") {
                // установим ssid
                ssid = (r.data["ssid"] as String?) ?: throw BadRequest("wrong ssid")
                // и найдем наш аккаунт в кэше
                account = GameServer.accountCache.get(ssid) ?: throw AuthorizationException()

                // выбраннй перс
                val selectedCharacterId: Int = (r.data["selectedCharacterId"] as Long).toInt()

                // load char
                val character = transaction {
                    Character.find { Characters.account eq account!!.id and Characters.id.eq(selectedCharacterId) }
                        .firstOrNull()
                        ?: throw BadRequest("character not found")
                }
                // создали игрока, его позицию
                val player = Player(character, this)

                // спавним игрока в мир, прогружаются гриды, активируются
                if (!player.pos.spawn()) {
                    throw BadRequest("failed spawn player into world")
                }

                player.loadGrids()

                this.player = player

                logger.debug("send welcome")
                send(GameResponse("general", "welcome to Origin ${ServerConfig.PROTO_VERSION}"))
            }
        } else {
            when (r.target) {
                // TODO delete
                "test" -> {
                    ack(r, "test")
                }
                // TODO delete
                "bye" -> {
                    connect.close(CloseReason(CloseReason.Codes.NORMAL, "said bye"))
                }
                else -> {
                    logger.warn("unknown target ${r.target}")
                }
            }
        }
    }

    fun disconnected() {
        player?.disconnected()
    }

    /**
     * ответ на запрос клиента
     */
    private suspend inline fun ack(req: GameRequest, d: Any? = null) {
        send(GameResponse(req.id, d))
    }

    suspend fun send(r: GameResponse) {
        logger.debug("send $r")
        connect.outgoing.send(Frame.Text(gsonSerializer.toJson(r)))
    }

    suspend fun kick() {
        logger.warn("kick")
        connect.close(CloseReason(CloseReason.Codes.NORMAL, "kicked"))
        player?.disconnected()
    }
}