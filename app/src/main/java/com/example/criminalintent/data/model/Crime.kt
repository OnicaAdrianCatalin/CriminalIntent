package com.example.criminalintent.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Crime(
    @PrimaryKey val id: Int = 0,
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false
)
