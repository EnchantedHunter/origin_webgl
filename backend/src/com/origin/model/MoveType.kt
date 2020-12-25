package com.origin.model

enum class MoveType {
    // используется только когда объект спавнится в мир, или телепорт в другое место
    SPAWN,

    // передвижение по суше
    WALK,

    // плывет по воде
    SWIMMING
}