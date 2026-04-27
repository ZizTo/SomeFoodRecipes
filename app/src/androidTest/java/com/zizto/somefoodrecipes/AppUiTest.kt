package com.zizto.somefoodrecipes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class AppUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testSearch_performsAndShowsResults() {
        composeTestRule.onNodeWithText("Search recipe (e.g., chicken)")
            .performTextInput("chicken")

        composeTestRule.onNodeWithText("Search").performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Chicken Handi", substring = true).isDisplayed()
        }
        composeTestRule.onNodeWithText("Chicken Handi", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun testFavoritesScreen_initiallyEmpty() {
        composeTestRule.onNodeWithText("Go to Favorites").performClick()
        composeTestRule.onNodeWithText("Favorite Recipes").assertIsDisplayed()
    }


    @Test
    fun testSearchScreen_displayed() {
        composeTestRule.onNodeWithText("Search recipe (e.g., chicken)")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Search")
            .assertIsDisplayed()
    }

    @Test
    fun testNavigateToDetailsScreen() {
        composeTestRule.onNodeWithText("Search recipe (e.g., chicken)")
            .performTextInput("chicken")
        composeTestRule.onNodeWithText("Search").performClick()
        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Chicken Handi", substring = true).isDisplayed()
        }

        composeTestRule.onNodeWithText("Chicken Handi", substring = true)
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Instructions:", substring = true).isDisplayed()
        }
        composeTestRule.onNodeWithText("Instructions:", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun testMarkFavoriteFood() {
        composeTestRule.onNodeWithText("Search recipe (e.g., chicken)")
            .performTextInput("chicken")
        composeTestRule.onNodeWithText("Search").performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Chicken Handi", substring = true).isDisplayed()
        }
        composeTestRule.onNodeWithText("Chicken Handi", substring = true)
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag("favoriteIcon").isDisplayed()
        }
        composeTestRule.onNodeWithTag("favoriteIcon")
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag("detailBackButton").isDisplayed()
        }
        composeTestRule.onNodeWithTag("detailBackButton")
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Go to Favorites").isDisplayed()
        }
        composeTestRule.onNodeWithText("Go to Favorites")
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Chicken Handi", substring = true).isDisplayed()
        }
        composeTestRule.onNodeWithText("Chicken Handi", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun testUnmarkFavoriteFood() {
        composeTestRule.onNodeWithText("Search recipe (e.g., chicken)")
            .performTextInput("chicken")
        composeTestRule.onNodeWithText("Search").performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Chicken Handi", substring = true).isDisplayed()
        }
        composeTestRule.onNodeWithText("Chicken Handi", substring = true)
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag("favoriteIcon").isDisplayed()
        }
        composeTestRule.onNodeWithTag("favoriteIcon")
            .assertExists()
            .performClick()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag("detailBackButton").isDisplayed()
        }
        composeTestRule.onNodeWithTag("detailBackButton")
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Go to Favorites").isDisplayed()
        }
        composeTestRule.onNodeWithText("Go to Favorites")
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithText("Favorite Recipes", substring = true).isDisplayed()
        }
        composeTestRule.onNodeWithText("Chicken Handi", substring = true)
            .assertIsNotDisplayed()
    }
}