package com.example.wifianalyser.models

enum class AccessPointFilterEnum(val value: Int) {
    ALL(0),
    TWO_G(1),
    FIVE_G(2);

    companion object {
        fun fromValue(value: Int): AccessPointFilterEnum {
            return values().firstOrNull { it.value == value } ?: ALL

        }
    }
}