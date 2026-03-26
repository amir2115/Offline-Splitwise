package com.encer.offlinesplitwise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResponsiveSmokeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun guestFlowShowsHomeAndSettingsTabs() {
        composeTestRule.onNodeWithText("ادامه در حالت آفلاین").assertIsDisplayed()
        composeTestRule.onNodeWithText("ادامه در حالت آفلاین").performClick()

        composeTestRule.onNodeWithText("خانه").assertIsDisplayed()
        composeTestRule.onNodeWithText("تنظیمات").assertIsDisplayed()

        composeTestRule.onNodeWithText("تنظیمات").performClick()
        composeTestRule.onNodeWithText("زبان").assertIsDisplayed()
    }
}
