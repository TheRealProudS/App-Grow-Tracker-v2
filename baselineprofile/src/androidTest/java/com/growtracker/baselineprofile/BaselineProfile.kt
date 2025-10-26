package com.growtracker.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfile {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun startup() = baselineRule.collect(
        packageName = "com.growtracker.app",
        // A few iterations to stabilize
        maxIterations = 8
    ) {
        // Start default activity and wait for content
        startActivityAndWait()
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5_000)
    }
}
