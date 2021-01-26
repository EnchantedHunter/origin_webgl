package com.origin.model.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Pine(entity: EntityObject) : Tree(entity) {
    override fun getResourcePath(): String {
        if (stage == 10) return "trees/pine/stump"
        return "trees/pine/$stage"
    }
}