package com.origin.model

import com.origin.model.move.Position
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.concurrent.ConcurrentHashMap

/**
 * весь игровой мир
 */
@ObsoleteCoroutinesApi
object World {
    /**
     * регионы (материки, истансы)
     */
    private val regions = ConcurrentHashMap<Int, Region>()

    /**
     * список активных гридов которые надо обновлять
     */
    private val activeGrids = ConcurrentHashMap.newKeySet<Grid>(9)

    /**
     * активные игроки которые залогинены в мир
     */
    private val players = ConcurrentHashMap<ObjectID, Player>()

    private fun getRegion(region: Int): Region {
        if (region < 0) throw RuntimeException("wrong grid region")
        return regions.computeIfAbsent(region) {
            val (w, h) = getRegionSize(region)
            Region(region, w, h)
        }
    }

    /**
     * получить грид по позиции
     */
    fun getGrid(pos: Position): Grid {
        return getRegion(pos.region).getLayer(pos.level).getGrid(pos.gridX, pos.gridY)
    }

    /**
     * получить грид по координатам
     */
    fun getGrid(region: Int, level: Int, gx: Int, gy: Int): Grid {
        return getRegion(region).getLayer(level).getGrid(gx, gy)
    }

    /**
     * добавить активный грид в список активных (для апдейта)
     */
    fun addActiveGrid(grid: Grid) {
        activeGrids.add(grid)
    }

    /**
     * удалить активный грид (больше не будет обновляться)
     */
    fun removeActiveGrid(grid: Grid) {
        activeGrids.remove(grid)
    }

    fun addPlayer(player: Player) {
        players[player.id] = player
    }

    fun removePlayer(player: Player) {
        players.remove(player.id)
    }

    suspend fun disconnectAllCharacters() {
        players.values.forEach {
            it.session.kick()
        }
    }

    private fun getRegionSize(region: Int): Pair<Int, Int> {
        return when (region) {
            0 -> Pair(50, 50) // TODO region sizes
            else -> throw RuntimeException("Unknown region $region")
        }
    }
}