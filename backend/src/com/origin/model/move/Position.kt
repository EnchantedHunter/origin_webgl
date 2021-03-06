package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.*
import com.origin.utils.GRID_FULL_SIZE
import com.origin.utils.GRID_SIZE
import com.origin.utils.TILE_SIZE
import com.origin.utils.Vec2i
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * позиция объекта в игровом мире
 */
@ObsoleteCoroutinesApi
class Position(
    initx: Int,
    inity: Int,
    var level: Int,
    var region: Int,
    var heading: Short,
    private val parent: GameObject,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Position::class.java)
    }

    constructor(ix: Int, iy: Int, pos: Position) : this(ix, iy, pos.level, pos.region, pos.heading, pos.parent)

    val point = Vec2i(initx, inity)

    val x get() = point.x
    val y get() = point.y

    /**
     * грид в котором находится объект
     * либо null если еще не привязан к гриду (не заспавнен)
     */
    lateinit var grid: Grid
        private set

    /**
     * координаты грида
     */
    val gridX get() = point.x / GRID_FULL_SIZE
    val gridY get() = point.y / GRID_FULL_SIZE

    /**
     * индекс тайла грида в котором находятся данные координаты
     */
    val tileIndex: Int
        get() {
            val p = point.mod(GRID_FULL_SIZE).div(TILE_SIZE)
            return p.x + p.y * GRID_SIZE
        }

    /**
     * заспавнить объект в мир
     */
    suspend fun spawn(): Boolean {
        if (::grid.isInitialized) {
            throw RuntimeException("pos.grid is already set, on spawn")
        }
        // берем грид и спавнимся через него
        val g = World.getGrid(this)

        val resp = CompletableDeferred<CollisionResult>()
        g.send(GridMsg.Spawn(parent, resp))
        val result = resp.await()

        // если успешно добавились в грид - запомним его у себя
        return if (result.result == CollisionResult.CollisionType.COLLISION_NONE) {
            grid = g
            true
        } else {
            false
        }
    }

    fun setGrid(grid: Grid) {
        this.grid = grid
    }

    fun setGrid() {
        this.grid = World.getGrid(this)
    }

    fun dist(other: Position): Double = point.dist(other.point)

    fun dist(px: Int, py: Int): Double = point.dist(px, py)

    /**
     * установка новых координат
     */
    suspend fun setXY(x: Int, y: Int) {
        logger.debug("setXY $x $y")

        // запомним координаты старого грида
        val oldgx = gridX
        val oldgy = gridY

        // поставим новые координаты
        this.point.x = x
        this.point.y = y

        // если координаты грида изменились
        if (oldgx != gridX || oldgy != gridY) {
            val old = grid
            // получим новый грид из мира
            grid = World.getGrid(this)
            if (parent is MovingObject) {
                // уведомим объект о смене грида
                parent.onGridChanged()
            }
            old.objects.remove(parent)
            grid.objects.add(parent)
        }
    }

    override fun toString(): String {
        return "{pos $level $x $y ${this.hashCode()} $parent $point ${point.x} ${point.y} }"
    }
}