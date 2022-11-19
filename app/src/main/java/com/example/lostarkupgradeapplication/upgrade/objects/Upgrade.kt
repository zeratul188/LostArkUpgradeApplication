package com.example.lostarkupgradeapplication.upgrade.objects

data class Upgrade(
    val type: String, // 무기, 방어구 (유형)
    val grade: Int, // 등급 (티어) - 계승 상태
    val level: Int, // 강화 단계
    val percent: Double, // 강화 확률
    val enforce: Int, // 파괴, 수호석 갯수
    val stone: Int, // 돌파석 갯수
    val ingredient: Int, // 융화 재료 갯수
    val fragments: Int, // 명예의 파편 갯수
    val gold: Int, // 골드 갯수
    val experience: Int, // 재련 경험치
    val book: Int, // 책 추가 확률
    val per_ad: Double, // 고급 숨 확률
    val per_rr: Double, // 희귀 숨 확률
    val per_hr: Double, // 영웅 숨 확률
    val ad: Int, // 고급 최대 갯수
    val rr: Int, // 희귀 최대 갯수
    val hr: Int, // 영웅 최대 갯수
    val item: Int // 장비 레벨
)
