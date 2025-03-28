package com.frogbubbletea.usthong.ui.screens

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.frogbubbletea.usthong.CourseScreenActivity
import com.frogbubbletea.usthong.R
import com.frogbubbletea.usthong.StarredScreenActivity
import com.frogbubbletea.usthong.data.Course
import com.frogbubbletea.usthong.data.Prefix
import com.frogbubbletea.usthong.data.PrefixType
import com.frogbubbletea.usthong.data.Semester
import com.frogbubbletea.usthong.data.sampleCourses
import com.frogbubbletea.usthong.data.samplePrefixes
import com.frogbubbletea.usthong.data.sampleSemesters
import com.frogbubbletea.usthong.network.ScrapingStatus
import com.frogbubbletea.usthong.network.scrapeCourses
import com.frogbubbletea.usthong.ui.composables.CourseCard
import com.frogbubbletea.usthong.ui.composables.CourseList
import com.frogbubbletea.usthong.ui.composables.ExploreMenu
import com.frogbubbletea.usthong.ui.theme.USThongTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Shows all courses under a certain prefix
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefixScreen() {
    // Sample data
//    val semesters = sampleSemesters
//     val prefixes = samplePrefixes
//    val courses = sampleCourses

    // Variables for course data
    var prefixes: List<Prefix> by rememberSaveable { mutableStateOf(listOf()) }  // Course code prefixes
    var semesters: List<Semester> by rememberSaveable { mutableStateOf(listOf()) }  // Semesters
    var courses: List<Course> by rememberSaveable { mutableStateOf(listOf()) }  // Courses
    var error: String by rememberSaveable { mutableStateOf("") }  // Error message

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = null
    )

    // Variables to control explore menu
    var showExploreMenu by rememberSaveable { mutableStateOf(false) }
    val exploreMenuState = rememberModalBottomSheetState()
    val exploreMenuScope = rememberCoroutineScope()
    var selectedSemester by rememberSaveable { mutableStateOf(
        Semester(
            name = "",
            code = 0
        )
    ) }
    var selectedPrefix by rememberSaveable { mutableStateOf(
        Prefix(
            name = "",
            type = PrefixType.UNDEFINED,
        )
    ) }

    // Current context of prefix screen activity
    val prefixScreenContext = LocalContext.current

    // Load course data
    var scraping by rememberSaveable { mutableStateOf(ScrapingStatus.LOADING) }
//    if (scraping == ScrapingStatus.LOADING) {
    LaunchedEffect(selectedSemester, selectedPrefix) {
//        scraping = ScrapingStatus.LOADING
        try {
            // Perform course data scraping
            val scrapeResult = scrapeCourses(
                prefix = if (selectedPrefix.name == "") null else selectedPrefix,
                semester = if (selectedSemester.code == 0) null else selectedSemester
            )

            // Unpack scraped data
            prefixes = scrapeResult.prefixes
            semesters = scrapeResult.semesters
            courses = scrapeResult.courses

//            println(courses)  // DEBUG PRINT

            // Assign first prefix and latest semester after initial scrape
//            if (selectedPrefix.name == "")
//                selectedPrefix = prefixes[0]
//            if (selectedSemester.code == 0)
//                selectedSemester = semesters[0]
            selectedPrefix = scrapeResult.scrapedPrefix
            selectedSemester = scrapeResult.scrapedSemester

            scraping = ScrapingStatus.SUCCESS
        } catch (e: Exception) {
            scraping = ScrapingStatus.ERROR
            error = e.stackTraceToString()
        }
//        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            // Make window avoid overlapping with display cutout in landscape mode
            .then(
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    Modifier.windowInsetsPadding(WindowInsets.displayCutout)
                else
                    Modifier
            ),
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            // Shrink top bar title as user scrolls down
//            val topBarCollapsedFraction = scrollBehavior.state.collapsedFraction
//            val topBarTitleTextStyle = lerp(
//                start = MaterialTheme.typography.headlineMedium,
//                stop = MaterialTheme.typography.titleMedium,
//                fraction = topBarCollapsedFraction
//            )
//            val topBarSubtitleTextStyle = lerp(
//                start = MaterialTheme.typography.bodyMedium,
//                stop = MaterialTheme.typography.bodySmall,
//                fraction = topBarCollapsedFraction
//            )

            TopAppBar(
                title = {
                    Surface(
                        onClick = { showExploreMenu = true},
                        color = Color.Transparent,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                // Course code prefix
                                Text(
                                    text = selectedPrefix.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                // Full name of course code prefix
//                                Text(
//                                    text = "Computer Science and Engineering",
//                                    style = MaterialTheme.typography.bodySmall,
//                                )
                                // Semester
                                Text(
                                    text = selectedSemester.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.material_icon_dropdown),
                                contentDescription = stringResource(id = R.string.explore_icon_desc),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    // Search button
//                    IconButton(
//                        onClick = { }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.material_icon_search),
//                            contentDescription = stringResource(id = R.string.search_icon_desc)
//                        )
//                    }

                    // Sort button
                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.material_icon_sort),
                            contentDescription = stringResource(id = R.string.sort_icon_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        // Explore menu
        if (showExploreMenu) {
            ExploreMenu(
                semesters = semesters,
                prefixes = prefixes,
                selectedSem = selectedSemester,
                selectedPrefix = selectedPrefix,
                onSelectStarred = {
                    exploreMenuScope.launch { exploreMenuState.hide() }.invokeOnCompletion {
                        if (!exploreMenuState.isVisible) {
                            showExploreMenu = false
                        }

                        val starredIntent =
                            Intent(prefixScreenContext, StarredScreenActivity::class.java)
                        prefixScreenContext.startActivity(starredIntent)
                    }
                },
                onSelectSem = { semester ->
                    scraping = ScrapingStatus.LOADING
                    selectedSemester = semester
                    exploreMenuScope.launch { exploreMenuState.hide() }.invokeOnCompletion {
                        if (!exploreMenuState.isVisible) {
                            showExploreMenu = false
                        }
                    }
                },
                onSelectPrefix = { prefix ->
                    scraping = ScrapingStatus.LOADING
                    selectedPrefix = prefix
                    exploreMenuScope.launch { exploreMenuState.hide() }.invokeOnCompletion {
                        if (!exploreMenuState.isVisible) {
                            showExploreMenu = false
                        }
                    }
                },
                onDismissRequest = {
                    exploreMenuScope.launch { exploreMenuState.hide() }.invokeOnCompletion {
                        if (!exploreMenuState.isVisible) {
                            showExploreMenu = false
                        }
                    }
                },
                sheetState = exploreMenuState,
            )
        }

//        when (scraping) {
//            ScrapingStatus.LOADING -> Unit
//            ScrapingStatus.ERROR -> Unit
//            ScrapingStatus.SUCCESS -> CourseList(
//                innerPadding = innerPadding,
//                courses = courses
//            )
//        }
        if (scraping == ScrapingStatus.LOADING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(32.dp),
                    )
                }
            }
        }

        if (scraping == ScrapingStatus.ERROR) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    Text(error)
                }
            }
        }

        AnimatedVisibility(
            visible = (scraping == ScrapingStatus.SUCCESS),
            enter = slideInVertically(
                initialOffsetY = { 120 }
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { 120 }
            ) + fadeOut()
        ) {
            CourseList(
                innerPadding = innerPadding,
                courses = courses,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun GreetingPreview() {
    USThongTheme {
        PrefixScreen()
    }
}