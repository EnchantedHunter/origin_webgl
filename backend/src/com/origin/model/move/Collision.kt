package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.utils.TILE_SIZE
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt

@ObsoleteCoroutinesApi
object Collision {
    /**
     * сколько тайлов до конца мира будут давать коллизию
     */
    private const val WORLD_BUFFER_SIZE = TILE_SIZE * 5

    /**
     * расстояние в единицах игровых координат между итерациями
     */
    private const val COLLISION_ITERATION_LENGTH = 2.0

    private val logger: Logger = LoggerFactory.getLogger(Collision::class.java)

    suspend fun process(
        toX: Int,
        toY: Int,
        dist: Double,
        obj: GameObject,
        list: Array<Grid>,
        isMove: Boolean,
    ): CollisionResult {
        //logger.debug("process to $toX $toY dist=$dist")

        // определяем вектор движения для отсечения объектов которые находятся за пределами вектора
//        val totalDist = obj.pos.point.dist(toX, toY)
//        val kd = if (totalDist == 0.0) 0.0 else dist / totalDist
//        val dp = Vec2i(toX, toY).sub(obj.pos.point).mul(kd).add(obj.pos.point) // TODO

        val dx = toX - obj.pos.x
        val dy = toY - obj.pos.y
        // прямоугольник по границам объекта захватывающий начальную и конечную точку движения
        val movingArea = obj.getBoundRect().clone().move(obj.pos.point)
            .extendSize(dist.roundToInt() + 5, dist.roundToInt() + 5)// extend(dp.x, dp.y) //

        // получаем список объектов для обсчета коллизий из списка гридов
        val filtered = list.flatMap { it ->
            // фильтруем список объектов. вернем только те которые ТОЧНО МОГУТ дать коллизию
            // те которые далеко или не попадают в прямоугольник движения - отсечем их
            it.objects.filter {
                // сами себе никогда не даем коллизию
                if (obj == it) return@filter false
                // дает ли объект коллизию?
                if (!it.isCollideWith(obj)) return@filter false
                // рект объекта
                val r = it.getBoundRect().clone().move(it.pos.point)
                // границы объекта должны быть в границах вектора движения объекта
                // то есть пересекаться с областью движения объекта
                movingArea.isIntersect(r)
            }
        }

        //logger.warn("filtered [${filtered.size}]:")
        filtered.forEach {
            //logger.debug("$it")
        }

        var curX: Double = obj.pos.x.toDouble()
        var curY: Double = obj.pos.y.toDouble()
        var newX: Double
        var newY: Double
        var needExit = false

        //logger.debug("obj $obj d $dx, $dy movingArea $movingArea")


        var distRemained = dist
        var oldD = 0.0
        var dd: Double
        var oldAd: Double = -1.0
        var counter = 0
        while (true) {
            counter++
            //logger.warn("CYCLE [$counter] =============================")


            // расстояние до конечной точки пути
            val actualDist: Double = distance(curX, curY, toX.toDouble(), toY.toDouble())

            val diffAd = if (oldAd < 0) actualDist else abs(actualDist - oldAd)



            dd = (abs(oldD - distRemained))
            oldD = distRemained
            //logger.debug("d remained=${String.format("%.2f", distRemained)} dd=${String.format("%.2f", dd)} " +
//            "ad=${String.format("%.2f", actualDist)} diffAd=${String.format("%.2f", diffAd)}")

            if (diffAd < 0.35) {
                if (isMove) {
                    obj.pos.setXY(curX.roundToInt(), curY.roundToInt())
                }
                return CollisionResult.NONE
            }

            when {
                // осталось слишком мало. считаем что пришли. коллизий не было раз здесь
                distRemained < 0.01 -> {
                    //logger.debug("d < 0.01 counter $counter")
                    if (isMove) {
                        obj.pos.setXY(curX.roundToInt(), curY.roundToInt())
                    }
                    return CollisionResult.NONE
                }
                (distRemained < COLLISION_ITERATION_LENGTH) -> {
                    val remained = distRemained
                    // осталось идти меньше одной итерации. очередная точка это конечная
                    val k: Double = remained / actualDist
                    distRemained -= remained
                    newX = curX + (toX - curX) * k
                    newY = curY + (toY - curY) * k

                    // после обсчета этой коллизии надо завершить цикл
                    needExit = true
                }
                else -> {
                    val k: Double = COLLISION_ITERATION_LENGTH / actualDist
                    distRemained -= COLLISION_ITERATION_LENGTH
                    newX = curX + (toX - curX) * k
                    newY = curY + (toY - curY) * k
                }
            }
            //logger.warn("cur ${String.format("%.2f", curX)}, ${String.format("%.2f", curY)} " +
//                    "-> new ${String.format("%.2f", newX)}, ${String.format("%.2f", newY)} distRemained=$distRemained")

            fun testObjCollision(
                isMove: Boolean,
            ): CollisionResult? {
                val newXInt: Int = newX.roundToInt()
                val newYInt: Int = newY.roundToInt()

                // хитбокс объекта который движется
                val movingRect = obj.getBoundRect().clone().move(newXInt, newYInt)
                //logger.warn("testObjCollision ${String.format("%.1f", newX)} ${String.format("%.1f", newY)}")
                //logger.debug("movingRect $movingRect")

                // проверяем коллизию с объектами
                val collisions = filtered.mapNotNull {
                    val ro = it.getBoundRect().clone().move(it.pos.point)
                    if (isMove) {
                        //logger.debug("test $ro $movingRect $it")
                    }
                    if (movingRect.isIntersect(ro)) {
                        if (isMove) {
                            //logger.warn("COLLISION!")
                            val oldNX = newX
                            val oldNY = newY

                            val ndx = newX - curX
                            val ndy = newY - curY
                            val ndd = distance(newX, newY, curX, curY)
                            //logger.warn("nd ${String.format("%.3f", ndx)} ${
//                                String.format("%.3f",
//                                    ndy)
//                            } ndd=${String.format("%.2f", ndd)}")
//
                            val threshold = 0.08
                            var cr = CollisionResult(CollisionResult.CollisionType.COLLISION_NONE, newX, newY, it)
                            if (abs(ndy) > threshold) {
                                newX = curX
                                val m = sign(ndy) * (ndd - abs(ndy))
                                newY += m
                                //logger.debug("try move Y $m")
                                val r1 = testObjCollision(false)
                                //logger.debug("r1 $r1")
                                cr = r1 ?: CollisionResult(CollisionResult.CollisionType.COLLISION_NONE, newX, newY, it)
                                newX = oldNX
                                newY = oldNY
                            }

                            if ((cr.isObject() || abs(ndy) <= threshold)
                                && abs(ndx) > threshold
                            ) {
                                newY = curY
                                val m = sign(ndx) * (ndd - abs(ndx))
                                newX += m
                                //logger.debug("try move X $m")
                                val r2 = testObjCollision(false)
                                //logger.debug("r2 $r2")

                                cr = r2 ?: CollisionResult(CollisionResult.CollisionType.COLLISION_NONE, newX, newY, it)
                                newX = oldNX
                                newY = oldNY
                            }
                            cr
                        } else {
                            CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, curX, curY, it)
                        }
                    } else {
                        null
                    }
                }
                if (isMove) {
                    //logger.debug("collisions [${collisions.size}] :")
                    var min: CollisionResult? = null
                    var max: CollisionResult? = null
                    collisions.forEach {
                        //logger.debug("$it")
                        if (it.isNone()) {
                            if (max == null) {
                                max = it
                            }
                        }
                        if (it.isObject()) {
                            min = it
                        }
                    }
                    return if (min == null) {
                        max
                    } else {
                        min
                    }
                } else {
                    var co: CollisionResult? = null
                    collisions.forEach {
                        if (it.isObject()) {
                            co = it
                        }
                    }
                    return co
                }
            }

            val result = testObjCollision(isMove)
            if (result != null) {
                if (result.isNone()) {
                    newX = result.px
                    newY = result.py
                } else {
                    //logger.debug("return by testObjCollision $result")
                    obj.pos.setXY(curX.roundToInt(), curY.roundToInt())
                    return result
                }
            }

            if (needExit) {
                //logger.warn("needExit")
                if (isMove) {
                    obj.pos.setXY(newX.roundToInt(), newY.roundToInt())
                }
                return CollisionResult.NONE
            }

            if (isMove) {
                if (counter > 25) {
                    if (isMove) {
                        obj.pos.setXY(curX.roundToInt(), curY.roundToInt())
                    }
                    return CollisionResult.NONE
                }
            }

            oldAd = actualDist

            curX = newX
            curY = newY
        }
    }

    private inline fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
}