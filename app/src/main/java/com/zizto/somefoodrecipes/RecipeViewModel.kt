package com.zizto.somefoodrecipes

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

class RecipeViewModel(private val api: MealApi, private val dao: MealDao) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<MealDto>>(emptyList())
    val searchResults: StateFlow<List<MealDto>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val favoriteMeals = dao.getAllFavorites()

    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.searchMeals(query)
                _searchResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getRecipeDetails(id: String): MealDto? {
        return try {
            val response = api.getMealDetails(id)
            response.meals?.firstOrNull()
        } catch (e: Exception) {
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
            } else {
                dao.insertFavorite(favMeal)
            }
        }
    }
}

class RecipeViewModelFactory(private val api: MealApi, private val dao: MealDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecipeViewModel(api, dao) as T
    }
}