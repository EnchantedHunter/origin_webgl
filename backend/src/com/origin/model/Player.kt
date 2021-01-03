package com.origin.model

import com.origin.entity.Character
import com.origin.net.model.GameSession
import kotlinx.coroutines.ObsoleteCoroutinesApi

enum class State {
    None, Connected, Disconnected
}

class PlayerMsg {
    class Connected
    class Disconnected
}

/**
 * инстанс персонажа игрока в игровом мире (игрок)
 */
@ObsoleteCoroutinesApi
class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    private val character: Character,

    val session: GameSession,
) : Human(character.id.value, character) {

    var state = State.None;

    /**
     * одежда (во что одет игрок)
     */
    val paperdoll: Paperdoll = Paperdoll(this)

    override suspend fun processMessages(msg: Any) {
        logger.debug("Player msg ${msg.javaClass.simpleName}")
        when (msg) {
            is PlayerMsg.Connected -> connected()
            is PlayerMsg.Disconnected -> disconnected()
            else -> super.processMessages(msg)
        }
    }

    /**
     * вызывается в самую последнюю очередь при спавне игрока в мир
     * когда уже все прогружено и заспавнено, гриды активированы
     */
    private fun connected() {
        state = State.Connected;
        World.addPlayer(this)
    }

    /**
     * игровой клиент (аккаунт) отключился от игрока
     */
    private suspend fun disconnected() {
        if (state == State.Disconnected) return

        World.removePlayer(this)

        // deactivate and unload grids
        unloadGrids()
        // удалить объект из мира
        remove()
        // завершаем актора
        actor.close()

        state = State.Disconnected
    }

}