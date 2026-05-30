package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("冰箱管理", appName)
  }

  @Test
  fun `launch main activity`() {
    val activityController = Robolectric.buildActivity(MainActivity::class.java)
    val activity = activityController.setup().get()
    assertNotNull(activity)
  }

  @Test
  fun `test database and viewmodel interaction`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewmodel = com.example.ui.FridgeViewModel(context.applicationContext as android.app.Application)
    assertNotNull(viewmodel.fridgeItems.value)
    assertNotNull(viewmodel.shoppingItems.value)
    assertNotNull(viewmodel.statistics.value)
  }

  @Test
  fun `test fridge main screen composition`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewmodel = com.example.ui.FridgeViewModel(context.applicationContext as android.app.Application)
    composeTestRule.setContent {
      com.example.ui.theme.MyApplicationTheme {
        com.example.ui.FridgeMainScreen(viewModel = viewmodel)
      }
    }
    composeTestRule.waitForIdle()

    // 1. Switch to "溫馨叮嚀" (Spirit) tab
    composeTestRule.onNodeWithText("溫馨叮嚀").performClick()
    composeTestRule.waitForIdle()

    // 2. Switch to "備忘清單" (Shopping) tab
    composeTestRule.onNodeWithText("備忘清單").performClick()
    composeTestRule.waitForIdle()

    // 3. Switch back to "冰箱食材"
    composeTestRule.onNodeWithText("冰箱食材").performClick()
    composeTestRule.waitForIdle()
  }
}
