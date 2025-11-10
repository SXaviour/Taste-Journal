package com.griffith.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishName: String,
    val kcal: Int? = null,
    val cookMinutes: Int? = null,
    val ingredients: String,
    val steps: String,
    val sourceLink: String? = null,
    val notes: String? = null,
    val dateCooked: Long = System.currentTimeMillis(),
    val cuisine: String? = null,
    val mealType: String? = null,
    val imageUri: String? = null,
    val rating: Int? = null
)