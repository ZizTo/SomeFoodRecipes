package com.zizto.somefoodrecipes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zizto.somefoodrecipes.data.FavoriteMeal
import com.zizto.somefoodrecipes.data.MealApi
import com.zizto.somefoodrecipes.data.MealDao
import com.zizto.somefoodrecipes.data.MealDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "RecipeViewModel"

class RecipeViewModel(private val api: MealApi, private val dao: MealDao) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<MealDto>>(emptyList())
    val searchResults: StateFlow<List<MealDto>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val favoriteMeals = dao.getAllFavorites()

    fun search(query: String) {
        Log.d(TAG, "Поиск рецептов по запросу: $query")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.searchMeals(query)
                Log.d(TAG, "Получено ${response.meals?.size ?: 0} результатов")
                _searchResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при поиске рецептов: $e")
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Поиск завершён, isLoading = false")
            }
        }
    }

    suspend fun getRecipeDetails(id: String): MealDto? {
        Log.d(TAG, "Загрузка деталей рецепта с ID: $id")
        return try {
            val response = api.getMealDetails(id)
            val meal = response.meals?.firstOrNull()
            if (meal != null) {
                Log.d(TAG, "Детали рецепта получены: ${meal.strMeal}")
            } else {
                Log.w(TAG, "Рецепт с ID $id не найден")
            }
            meal
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке деталей рецепта: $e")
            null
        }
    }

    fun isFavorite(id: String) = dao.isFavorite(id)

    fun toggleFavorite(meal: MealDto, isFav: Boolean) {
        viewModelScope.launch {
            val favMeal = FavoriteMeal(
                idMeal = meal.idMeal,
                name = meal.strMeal,
                imageUrl = meal.strMealThumb,
                instructions = meal.strInstructions ?: ""
            )
            if (isFav) {
                dao.deleteFavorite(favMeal)
                Log.d(TAG, "Рецепт ${meal.strMeal} удалён из избранного")
            } else {
                dao.insertFavorite(favMeal)
                Log.d(TAG, "Рецепт ${meal.strMeal} добавлен в избранное")
            }
        }
    }
}

class RecipeViewModelFactory(private val api: MealApi, private val dao: MealDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("RecipeViewModelFactory", "Создание экземпляра RecipeViewModel")
        return RecipeViewModel(api, dao) as T
    }
}