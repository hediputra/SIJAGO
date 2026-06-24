package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ExecutiveAdminDashboard
import com.example.ui.SijagoViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.data.NewsItem
import com.example.ui.NewsScreen
import com.example.ui.BudgetRechartsVisualizer
import com.example.ui.AttendanceScreen
import com.example.ui.ReportScreen
import com.example.data.AttendanceRecord
import com.example.data.ReportItem
import java.text.DecimalFormat
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SIJAGO", appName)
  }

  @Test
  fun testOfflineSimulationToggle() = runBlocking {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SijagoViewModel(application)

    // Initially simulated offline is false
    assertFalse(viewModel.isSimulatedOffline.value)

    // Toggle simulation to true
    viewModel.setSimulatedOffline(true)
    assertTrue(viewModel.isSimulatedOffline.value)
    
    // Check that isOnline becomes false
    assertFalse(viewModel.isOnline.value)

    // Toggle simulation back to false
    viewModel.setSimulatedOffline(false)
    assertFalse(viewModel.isSimulatedOffline.value)
  }

  @Test
  fun testExecutiveAdminDashboardRendering() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SijagoViewModel(application)

    composeTestRule.setContent {
      MyApplicationTheme {
        ExecutiveAdminDashboard(
          viewModel = viewModel,
          citizenCount = 1200,
          kkCount = 350,
          maleCount = 610,
          femaleCount = 590,
          poorCount = 45,
          pendingLettersCount = 3,
          onNavigateToTab = {}
        )
      }
    }

    // Verify presence of dashboard widgets using their respective test tags in layout tree
    composeTestRule.onNodeWithTag("executive_dashboard_container").assertExists()
    composeTestRule.onNodeWithTag("resident_stats_card").assertExists()
    composeTestRule.onNodeWithTag("service_requests_stats_card").assertExists()
    composeTestRule.onNodeWithTag("financial_stats_card").assertExists()
  }

  @Test
  fun testGisMapScreenLayoutRendering() {
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.GisMapScreen(role = "Admin")
      }
    }

    // Verify main components render successfully
    composeTestRule.onNodeWithTag("gis_screen_root").assertExists()
    composeTestRule.onNodeWithTag("gis_map_viewport").assertExists()
    composeTestRule.onNodeWithTag("gis_search_field").assertExists()
    composeTestRule.onNodeWithTag("gis_add_marker_button").assertExists()
  }

  @Test
  fun testNewsScreenFiltersRenderCorrectly() {
    val sampleNews = listOf(
      NewsItem(
        id = 101,
        title = "Rapat RT Se-Desa Sumber Rejo",
        category = "Agenda",
        content = "Rapat rutin kuartal kedua untuk membahas infrastruktur jalan tani.",
        date = "2026-06-25",
        imageUrl = ""
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        NewsScreen(
          newsList = sampleNews,
          role = "Masyarakat",
          onAddNews = {},
          onDeleteNews = {}
        )
      }
    }

    // Assert search input and date picker button exist in hierarchy
    composeTestRule.onNodeWithTag("news_search_input").assertExists()
    composeTestRule.onNodeWithTag("news_date_filter_button").assertExists()
  }

  @Test
  fun testBudgetRechartsVisualizerRendering() {
    composeTestRule.setContent {
      MyApplicationTheme {
        BudgetRechartsVisualizer(
          formatter = DecimalFormat("#,###"),
          isDark = false
        )
      }
    }

    // Verify visualizer main elements exist
    composeTestRule.onNodeWithTag("budget_visualizer_card").assertExists()
    composeTestRule.onNodeWithTag("tab_budget_allocation").assertExists()
    composeTestRule.onNodeWithTag("tab_budget_realization").assertExists()
    composeTestRule.onNodeWithTag("budget_transparency_summary").assertExists()
  }

  @Test
  fun testAttendanceScreenElementsRenderCorrectly() {
    val sampleAttendance = listOf(
      AttendanceRecord(
        id = 1,
        staffName = "Drs. Budi Santoso",
        date = "2026-06-24",
        checkInTime = "08:00:15",
        checkOutTime = "-",
        locationName = "Kantor Kepala Desa Sumber Rejo",
        latitude = -7.4523,
        longitude = 110.3654,
        label = "Hadir"
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        AttendanceScreen(
          attendanceList = sampleAttendance,
          onCheckIn = { _, _, _, _ -> },
          onCheckOut = { _ -> }
        )
      }
    }

    // Verify main interactive elements exist in the view
    composeTestRule.onNodeWithTag("attendance_staff_name_input").assertExists()
    composeTestRule.onNodeWithTag("scan_biometric_face_btn").assertExists()
    composeTestRule.onNodeWithTag("attendance_checkin_button").assertExists()
  }

  @Test
  fun testReportScreenElementsAndDashboardRendering() {
    val sampleReports = listOf(
      ReportItem(
        id = 1,
        title = "Pipa Air Bersih Bocor",
        description = "Saluran utama ke RT 04 pecah sehingga warga kesulitan air bersih.",
        category = "Infrastruktur",
        reporterName = "Ahmad Sobari",
        contact = "0812334455",
        status = "MASUK",
        isSynced = true
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ReportScreen(
          reports = sampleReports,
          onSubmitReport = { _, _, _, _, _, _ -> },
          role = "Masyarakat",
          onUpdateStatus = { _, _ -> },
          isOnline = true,
          isSimulatedOffline = false,
          onToggleOffline = {}
        )
      }
    }

    // Verify container and offline simulation banner exist
    composeTestRule.onNodeWithTag("report_screen_container").assertExists()
    composeTestRule.onNodeWithTag("offline_simulation_banner").assertExists()

    // Verify submission form elements exist
    composeTestRule.onNodeWithTag("report_input_form_card").assertExists()
    composeTestRule.onNodeWithTag("report_title_input").assertExists()
    composeTestRule.onNodeWithTag("report_desc_input").assertExists()
    composeTestRule.onNodeWithTag("report_category_dropdown_trigger").assertExists()
    composeTestRule.onNodeWithTag("upload_attachment_button").assertExists()
    composeTestRule.onNodeWithTag("submit_report_button").assertExists()

    // Scroll the lazy column container to the stats card node
    composeTestRule.onNodeWithTag("report_screen_container").performScrollToNode(hasTestTag("stats_card_total"))
    composeTestRule.onNodeWithTag("stats_card_total").assertExists()
    composeTestRule.onNodeWithTag("stats_card_masuk").assertExists()
    composeTestRule.onNodeWithTag("stats_card_proses").assertExists()
    composeTestRule.onNodeWithTag("stats_card_selesai").assertExists()

    // Scroll the lazy column container to the report card item node
    composeTestRule.onNodeWithTag("report_screen_container").performScrollToNode(hasTestTag("report_card_item_1"))
    composeTestRule.onNodeWithTag("report_card_item_1").assertExists()
  }

  @Test
  fun testHomeScreenSearchFunctionality() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SijagoViewModel(application)

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.HomeScreen(
          viewModel = viewModel,
          citizenCount = 100,
          kkCount = 30,
          maleCount = 50,
          femaleCount = 50,
          poorCount = 10,
          onNavigateToTab = {},
          showAddUmkmDialog = {},
          showAddProjectDialog = {}
        )
      }
    }

    // Verify search input exists
    composeTestRule.onNodeWithTag("village_portal_search_input").assertExists()

    // Enter positive search query "Domisili"
    composeTestRule.onNodeWithTag("village_portal_search_input").performTextInput("Domisili")

    // Verify search results container and matched template exist
    composeTestRule.onNodeWithTag("search_results_container").assertExists()
    composeTestRule.onNodeWithTag("search_result_doc_template_Surat_Keterangan_Domisili").assertExists()

    // Enter negative query for "xyz999" (gibberish query)
    composeTestRule.onNodeWithTag("clear_search_button").performClick()
    composeTestRule.onNodeWithTag("village_portal_search_input").performTextInput("xyz999")

    // Verify no results layout is displayed
    composeTestRule.onNodeWithTag("search_no_results").assertExists()
  }

  @Test
  fun testEventCalendarFunctionality() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SijagoViewModel(application)

    // Explicitly add an event for Day 24 so that it is guaranteed to be present immediately
    viewModel.addVillageEvent(
      title = "Posyandu Balita Suka Makmur",
      description = "Pemeriksaan kesehatan rutin untuk balita.",
      date = "2026-06-24",
      time = "09:00",
      location = "Posyandu Dusun Suka Makmur",
      type = "ACTIVITY",
      organizer = "Kader Posyandu & PKK"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.EventCalendarComponent(viewModel = viewModel)
      }
    }

    // Wait for coroutines and compose layout to settle
    composeTestRule.waitForIdle()

    // Verify calendar navigation buttons exist
    composeTestRule.onNodeWithTag("village_portal_calendar_prev_month").assertExists()
    composeTestRule.onNodeWithTag("village_portal_calendar_next_month").assertExists()

    // Verify category filter chips exist
    composeTestRule.onNodeWithTag("village_portal_calendar_type_filter_all").assertExists()
    composeTestRule.onNodeWithTag("village_portal_calendar_type_filter_meeting").assertExists()
    composeTestRule.onNodeWithTag("village_portal_calendar_type_filter_activity").assertExists()
    composeTestRule.onNodeWithTag("village_portal_calendar_type_filter_deadline").assertExists()

    // Click on Day 24
    composeTestRule.onNodeWithTag("village_portal_calendar_day_card_24").performClick()
    composeTestRule.waitForIdle()

    // Verify that the event row card exists for Day 24 event
    // The test ID of the card will match the event title or ID. We can assert that any card with tag matching Day 24's event renders.
    composeTestRule.onNodeWithTag("village_portal_calendar_event_card_1").assertExists()
  }
}

