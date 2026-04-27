package com.zizto.somefoodrecipes;

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.zizto.somefoodrecipes.data.AppDatabase
import com.zizto.somefoodrecipes.data.MealApi



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = MealApi.create()
        val dao = AppDatabase.getDatabase(applicationContext).mealDao()
        val factory = RecipeViewModelFactory(api, dao)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val viewModel: RecipeViewModel = viewModel(factory = factory)
                    val context = LocalContext.current

                    var isInternetConnected by remember { mutableStateOf(isNetworkAvailable(context)) }

                    if (!isInternetConnected) {
                        NoInternetDialog(onRetry = {
                            isInternetConnected = isNetworkAvailable(context)
                        })
                    } else {
                        RecipeApp(navController, viewModel)
                    }
                }
            }
        }
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun NoInternetDialog(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = { Text(stringResource(R.string.no_internet_title)) },
        text = { Text(stringResource(R.string.no_internet_msg)) },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    )
}

@Composable
fun RecipeApp(navController: NavHostController, viewModel: RecipeViewModel) {
    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(viewModel, onNavigateToDetails = { id ->
                navController.navigate("details/$id")
            }, onNavigateToFavorites = {
                navController.navigate("favorites")
            })
        }
        composable("details/{mealId}") { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId") ?: ""
            RecipeDetailScreen(
                mealId = mealId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("favorites") {
            FavoritesScreen(viewModel, onNavigateToDetails = { id ->
                navController.navigate("details/$id")
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: RecipeViewModel, onNavigateToDetails: (String) -> Unit, onNavigateToFavorites: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.search(query)
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.search(query)
                keyboardController?.hide()
            }) {
                Text(stringResource(R.string.search_button))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToFavorites, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.go_to_favorites))
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(results) { meal ->
                    ListItem(
                        headlineContent = { Text(meal.strMeal) },
                        leadingContent = {
                            AsyncImage(
                                model = meal.strMealThumb,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                        },
                        modifier = Modifier.clickable { onNavigateToDetails(meal.idMeal) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(
    mealId: String,
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit
) {
    var meal by remember { mutableStateOf<com.zizto.somefoodrecipes.data.MealDto?>(null) }
    val isFavorite by viewModel.isFavorite(mealId).collectAsState(initial = false)

    LaunchedEffect(mealId) {
        meal = viewModel.getRecipeDetails(mealId)
    }

    meal?.let { currentMeal ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detailBackButton")) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
                Text(
                    text = currentMeal.strMeal,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                // Иконка избранного перенесена сюда или может остаться ниже
                IconButton(onClick = { viewModel.toggleFavorite(currentMeal, isFavorite) }, modifier = Modifier.testTag("favoriteIcon")) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            AsyncImage(
                model = currentMeal.strMealThumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(250.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.instructions), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentMeal.strInstructions ?: stringResource(R.string.no_instructions))
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: RecipeViewModel, onNavigateToDetails: (String) -> Unit) {
    val favorites by viewModel.favoriteMeals.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(favorites) { meal ->
                ListItem(
                    headlineContent = { Text(meal.name) },
                    leadingContent = {
                        AsyncImage(
                            model = meal.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp)
                        )
                    },
                    modifier = Modifier.clickable { onNavigateToDetails(meal.idMeal) }
                )
            }
        }
    }
}