package com.origin.model

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class StaticObject(entity: EntityObject) :
    GameObject(entity.id.value, entity.x, entity.y, entity.level, entity.region, entity.heading) {

    val type = entity.type
    override fun getBoundRect(): Rect {
        // TODO getBoundRect
        return Rect(10)
    }
}