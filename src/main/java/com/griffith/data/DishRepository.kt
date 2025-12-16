package com.griffith.data

class DishRepository(private val dao: DishDao) {
    fun all() = dao.all()
    fun byId(id: Long) = dao.observe(id)
    suspend fun save(d: Dish) = dao.upsert(d)
    suspend fun delete(d: Dish) = dao.delete(d)
    fun recent(n: Int) = dao.recent(n)
    fun forgotten(min: Int, cutoff: Long, n: Int) = dao.forgotten(min, cutoff, n)
    fun top(n: Int) = dao.top(n)

    suspend fun random() = dao.random()
    suspend fun clear() = dao.clear()


}