package com.zizto.somefoodrecipes

import android.util.Log
import app.cash.turbine.test
import com.zizto.somefoodrecipes.data.MealApi
import com.zizto.somefoodrecipes.data.MealDao
import com.zizto.somefoodrecipes.data.MealDto
import com.zizto.somefoodrecipes.data.MealResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.collections.emptyList

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var api: MealApi
    private lateinit var dao: MealDao
    private lateinit var viewModel: RecipeViewModel

    private val testMealDto = MealDto(
        idMeal = "52772",
        strMeal = "Teriyaki Chicken Casserole",
        strMealThumb = "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
        strInstructions = "Cook instructions..."
    )

    @Before
    fun setup() {
        // Мокаем статические методы Android Log
        mockkStatic("android.util.Log")
        every { Log.d(any(), any()) } returns 0
        every { Log.d(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.i(any(), any(), any()) } returns 0
        every { Log.w(any(), any(), any()) } returns 0
        every { Log.w(any(), any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.v(any(), any(), any()) } returns 0

        api = mockk()
        dao = mockk()

        every { dao.getAllFavorites() } returns flowOf(emptyList())
        every { dao.isFavorite(any()) } returns flowOf(false)

        viewModel = RecipeViewModel(api, dao)
    }

    @Test
    fun `search success updates searchResults state`() = runTest {
        val response = MealResponse(listOf(testMealDto))
        coEvery { api.searchMeals("chicken") } returns response

        viewModel.searchResults.test {
            assertEquals(emptyList<MealDto>(), awaitItem())

            viewModel.search("chicken")

            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Teriyaki Chicken Casserole", result[0].strMeal)
        }
    }

    @Test
    fun `toggleFavorite inserts meal when not favorite`() = runTest {
        val isFav = false
        coEvery { dao.insertFavorite(any()) } returns Unit
        coEvery { dao.deleteFavorite(any()) } returns Unit

        viewModel.toggleFavorite(testMealDto, isFav)

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dao.insertFavorite(match {
                it.idMeal == testMealDto.idMeal && it.name == testMealDto.strMeal
            })
        }
        coVerify(exactly = 0) { dao.deleteFavorite(any()) }
    }

    @Test
    fun `toggleFavorite deletes meal when is favorite`() = runTest {
        val isFav = true
        coEvery { dao.insertFavorite(any()) } returns Unit
        coEvery { dao.deleteFavorite(any()) } returns Unit

        viewModel.toggleFavorite(testMealDto, isFav)

        advanceUntilIdle()

        coVerify(exactly = 1) { dao.deleteFavorite(any()) }
        coVerify(exactly = 0) { dao.insertFavorite(any()) }
    }

    @Test
    fun `isFavorite delegates to dao`() = runTest {
        val expectedFlow = flowOf(true)
        every { dao.isFavorite("52772") } returns expectedFlow

        val resultFlow = viewModel.isFavorite("52772")

        resultFlow.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}