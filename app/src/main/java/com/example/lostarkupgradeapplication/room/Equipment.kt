package com.example.lostarkupgradeapplication.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.floor

@Entity
data class Equipment (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") var name: String, //장비 이름
    @ColumnInfo(name = "type") var type: String, //장비 종류
    @ColumnInfo(name = "level") var level: Int, //장비 재련 단계
    @ColumnInfo(name = "itemlevel") var itemlevel: Int, //장비 아이템 레벨
    @ColumnInfo(name = "power") var power: Double, //장비 장인의 기운
    @ColumnInfo(name = "honer") var honer: Int, //장비 명예의 파편 경험치 (현재 수치)
    @ColumnInfo(name = "statue") var statue: Int, //장비 티어 (계승 상태)
    @ColumnInfo(name = "stack") var stack: Int //장비 누른 횟수
) {
    fun getProgress(): Int {
        return power?.times(100)?.toInt() ?: 0
    }

    fun getFloorString(): String {
        return (floor(power*100)/100).toString()
    }
}
