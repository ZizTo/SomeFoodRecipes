package com.zizto.somefoodrecipes.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MealDaoTest {

    private lateinit var dao: MealDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.mealDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertFavoriteAndGetAllFavorites() = runTest {
        val meal = FavoriteMeal(
            idMeal = "1",
            name = "Pizza Margherita",
            imageUrl = "https://example.com/pizza.jpg",
            instructions = "Bake at 200C"
        )

        dao.insertFavorite(meal)

        val allFavorites = dao.getAllFavorites().first()

        assertEquals(1, allFavorites.size)
        assertEquals("Pizza Margherita", allFavorites[0].name)
        assertEquals("1", allFavorites[0].idMeal)
    }

    @Test
    fun deleteFavorite_removesItem() = runTest {
        val meal = FavoriteMeal(
            idMeal = "2",
            name = "Burger",
            imageUrl = "https://example.com/burger.jpg",
            instructions = "Grill meat"
        )

        dao.insertFavorite(meal)
        assertEquals(1, dao.getAllFavorites().first().size)

        dao.deleteFavorite(meal)

        val allFavorites = dao.getAllFavorites().first()
        assertTrue(allFavorites.isEmpty())
    }

    @Test
    fun isFavorite_returnsCorrectStatus() = runTest {
        val meal = FavoriteMeal(
            idMeal = "99",
            name = "Sushi",
            imageUrl = "https://example.com/sushi.jpg",
            instructions = "Roll it"
        )

        dao.insertFavorite(meal)

        val exists = dao.isFavorite("99").first()
        assertTrue(exists)

        val notExists = dao.isFavorite("111").first()
        assertFalse(notExists)
    }

    @Test
    fun insertWithSameId_replacesExistingRecord() = runTest {
        val originalMeal = FavoriteMeal(
            idMeal = "5",
            name = "Old Name",
            imageUrl = "url1",
            instructions = "inst1"
        )

        val updatedMeal = FavoriteMeal(
            idMeal = "5",
            name = "New Name",
            imageUrl = "url2",
            instructions = "inst2"
        )

        dao.insertFavorite(originalMeal)

        dao.insertFavorite(updatedMeal)

        val allFavorites = dao.getAllFavorites().first()

        assertEquals(1, allFavorites.size)
        assertEquals("New Name", allFavorites[0].name)
        assertEquals("url2", allFavorites[0].imageUrl)
    }
}