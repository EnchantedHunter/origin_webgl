package com.origin.model

import com.origin.ServerConfig
import com.origin.utils.ObjectID
import com.origin.utils.Vec2i
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

/**
 * гуманоид
 * обладает зрением (видимые объекты о которых "знает")
 */
@ObsoleteCoroutinesApi
abstract class Human(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Int) :
    MovingObject(id, x, y, level, region, heading) {

    /**
     * дистанция на которой мы видим объекты
     * может изменяться динамически (ночью видим хуже)
     */
    private var visibleDistance = 500

    /**
     * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
     * любое добавление в этот список, а равно как и удаление из него должно быть
     * синхронизировано с клиентом
     */
    private val knownList by lazy { KnownList(this) }

    /**
     * последняя позиция в которой было обновление видимых объектов
     * нужно чтобы часто не обновлять список видимых (слишком накладно)
     */
    private var lastPosUpdateVisible: Vec2i? = null

    /**
     * добавили объект в грид в котором находится объект
     */
    override suspend fun onObjectAdded(obj: GameObject) {
        if (isObjectVisibleForMe(obj)) {
            knownList.addKnownObject(obj)
        }
    }

    /**
     * грид говорит что какой то объект был удален
     */
    override suspend fun onObjectRemoved(obj: GameObject) {
        logger.debug("human onObjectRemoved $obj")
        knownList.removeKnownObject(obj)
    }

    /**
     * вижу ли я указаный объект
     */
    private fun isObjectVisibleForMe(obj: GameObject): Boolean {
        // себя всегда видим!
        return obj.id == this.id || pos.dist(obj.pos) < visibleDistance
    }

    /**
     * обновить список видимых объектов
     * все новые что увидим - отправятся клиенту. старые что перестали видеть - будут удалены
     * @param force принудительно, иначе проверка будет только если отошли на значительное расстояние от точки последней проверки
     */
    suspend fun updateVisibleObjects(force: Boolean) {
        // TODO updateVisibleObjects

        if (force || (lastPosUpdateVisible != null
                    && pos.point != lastPosUpdateVisible
                    && pos.point.dist(lastPosUpdateVisible!!) > ServerConfig.VISIBLE_UPDATE_DISTANCE)
        ) {
            // запомним те объекты которые видимы при текущем апдейте
            val newList = LinkedList<GameObject>()

            var newCounter = 0
            // проходим по всем гридам в которых находимся
            for (grid in grids) {
                // TODO grid objects
                newCounter++
            }

            // какие объекты больше не видимы?
            val del = LinkedList<GameObject>()
            knownList.getKnownObjects().forEach {
                // если в новом списке нет - значит больше не видим,
                // пометим на удаление
                if (!newList.contains(it)) {
                    del.add(it)
                }
            }
            // удалим объекты которые больше не видим
            del.forEach {
                knownList.removeKnownObject(it)
            }

            lastPosUpdateVisible = pos.point.clone()
            logger.debug("updateVisibleObjects $this new=$newCounter vis=${newList.size} del=${del.size}")
        }
    }
}