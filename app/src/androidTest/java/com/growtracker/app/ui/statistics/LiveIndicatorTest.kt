package com.growtracker.app.ui.statistics

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class LiveIndicatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun liveIndicator_isOnForEvenSeconds() {
        composeTestRule.setContent {
            LiveIndicator(nowTick = 2_000L) // even second => on
        }
        composeTestRule.onNodeWithTag("live_dot")
            .assert(hasContentDescription("live_on"))
    }

    @Test
    fun liveIndicator_isOffForOddSeconds() {
        composeTestRule.setContent {
            LiveIndicator(nowTick = 3_000L) // odd second => off
        }
        composeTestRule.onNodeWithTag("live_dot")
            .assert(hasContentDescription("live_off"))
    }
}
