package com.movieapp.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.movieapp.app.AppSideEffect
import com.movieapp.app.resources.Res
import com.movieapp.app.resources.nav_home
import com.movieapp.app.resources.nav_library
import com.movieapp.app.resources.nav_profile
import com.movieapp.app.resources.nav_search
import com.movieapp.config.AppConfig
import com.movieapp.core.ui.components.CinematicSharedContentOrigin
import com.movieapp.core.ui.components.LocalMovieAppAnimatedVisibilityScope
import com.movieapp.core.ui.components.LocalMovieAppSharedContentOrigin
import com.movieapp.core.ui.components.LocalMovieAppSharedTransitionScope
import com.movieapp.feature.actors.screens.actor.ActorDetailScreen
import com.movieapp.feature.collections.screens.collections.CollectionDetailScreen
import com.movieapp.feature.movies.screens.browseall.BrowseAllRoute as BrowseAllScreenRoute
import com.movieapp.feature.movies.screens.favorites.FavoritesGridRoute
import com.movieapp.feature.movies.screens.home.HomeScreen
import com.movieapp.feature.movies.screens.movie.MovieDetailScreen
import com.movieapp.feature.movies.screens.upcoming.UpcomingCalendarScreen
import com.movieapp.feature.movies.screens.watchlist.WatchlistGridRoute
import com.movieapp.feature.profile.screens.profile.ProfileScreen
import com.movieapp.feature.series.screens.series.SeriesDetailScreen
import com.movieapp.feature.settings.screens.AboutScreen
import com.movieapp.feature.settings.screens.AccountScreen
import com.movieapp.feature.settings.screens.CatalogSettingsScreen
import com.movieapp.feature.settings.screens.HelpScreen
import com.movieapp.feature.settings.screens.LanguageScreen
import com.movieapp.feature.settings.screens.SettingsHomeScreen
import com.movieapp.feature.settings.screens.StorageScreen
import com.movieapp.feature.settings.screens.ThemeScreen
import com.movieapp.ui.gallery.ImageGalleryScreen
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun MovieAppNavigation(appSideEffects: Flow<AppSideEffect>) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val activeSharedContentOrigin = remember { mutableStateOf<CinematicSharedContentOrigin?>(null) }

    LaunchedEffect(navController, snackbarHostState) {
        appSideEffects.collect { effect ->
            when (effect) {
                is AppSideEffect.Navigate -> navController.navigate(effect.route)
                is AppSideEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(getString(effect.message))
            }
        }
    }

    val items =
        listOf(
            BottomNavItem(HomeRoute::class, Res.string.nav_home, Icons.Filled.Home),
            BottomNavItem(SearchRoute::class, Res.string.nav_search, Icons.Filled.Search),
            BottomNavItem(
                LibraryRoute::class,
                Res.string.nav_library,
                Icons.Filled.Collections,
            ),
            BottomNavItem(ProfileRoute::class, Res.string.nav_profile, Icons.Filled.Person),
        )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showSuite =
        currentDestination?.hierarchy?.any { dest ->
            items.any { dest.hasRoute(it.routeClass) }
        } == true
    val suiteState = rememberNavigationSuiteScaffoldState()

    LaunchedEffect(showSuite) {
        if (showSuite) suiteState.show() else suiteState.hide()
    }

    NavigationSuiteScaffold(
        state = suiteState,
        navigationSuiteItems = {
            if (showSuite) {
                items.forEach { item ->
                    item(
                        selected =
                            currentDestination?.hierarchy?.any { it.hasRoute(item.routeClass) } ==
                                true,
                        onClick = {
                            when (item.routeClass) {
                                HomeRoute::class ->
                                    navController.navigate(HomeRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                SearchRoute::class ->
                                    navController.navigate(SearchRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                LibraryRoute::class ->
                                    navController.navigate(LibraryRoute()) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                ProfileRoute::class ->
                                    navController.navigate(ProfileRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                            }
                        },
                        icon = {
                            Icon(item.icon, contentDescription = stringResource(item.titleRes))
                        },
                        label = {
                            NavigationSuiteLabel(text = stringResource(item.titleRes))
                        },
                    )
                }
            }
        },
    ) {
        // Status bars at root; NavigationSuiteScaffold owns nav-bar insets.
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                CompositionLocalProvider(
                    LocalMovieAppSharedTransitionScope provides this,
                    LocalMovieAppSharedContentOrigin provides activeSharedContentOrigin,
                ) {
                    NavHost(navController = navController, startDestination = HomeRoute) {
                        composable<HomeRoute> {
                            SharedTransitionDestination {
                                HomeScreen(
                                    onMovieClick = { movieId ->
                                        navController.navigate(MovieDetailRoute(movieId))
                                    },
                                    onSeriesClick = { seriesId ->
                                        navController.navigate(SeriesDetailRoute(seriesId))
                                    },
                                    onSeeAllClick = { category ->
                                        navController.navigate(BrowseAllRoute(category.name))
                                    },
                                    onUpcomingClick = { navController.navigate(UpcomingRoute) },
                                )
                            }
                        }
                        composable<SearchRoute> {
                            SharedTransitionDestination {
                                SearchListDetailPane(
                                    onMovieSelected = {
                                        navController.navigate(MovieDetailRoute(it))
                                    },
                                    onSeriesSelected = {
                                        navController.navigate(SeriesDetailRoute(it))
                                    },
                                    onActorSelected = {
                                        navController.navigate(ActorDetailRoute(it))
                                    },
                                )
                            }
                        }
                        composable<LibraryRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<LibraryRoute>()
                            SharedTransitionDestination {
                                LibraryListDetailPane(
                                    initialSection =
                                        runCatching { LibrarySection.valueOf(route.section) }
                                            .getOrDefault(LibrarySection.Watchlist),
                                    onSectionSelected = {},
                                    onCollectionClick = { collectionId ->
                                        navController.navigate(CollectionDetailRoute(collectionId))
                                    },
                                    onDiscoverClick = { navController.navigate(SearchRoute) },
                                    onSeeAllMoviesClick = {
                                        navController.navigate(WatchlistAllRoute("movies"))
                                    },
                                    onSeeAllSeriesClick = {
                                        navController.navigate(WatchlistAllRoute("series"))
                                    },
                                    onSeeAllFavoritesMoviesClick = {
                                        navController.navigate(FavoritesAllRoute("movies"))
                                    },
                                    onSeeAllFavoritesSeriesClick = {
                                        navController.navigate(FavoritesAllRoute("series"))
                                    },
                                    onSeeAllActorsClick = {
                                        navController.navigate(FavoritesAllRoute("actors"))
                                    },
                                    onMovieSelected = {
                                        navController.navigate(MovieDetailRoute(it))
                                    },
                                    onSeriesSelected = {
                                        navController.navigate(SeriesDetailRoute(it))
                                    },
                                    onActorSelected = {
                                        navController.navigate(ActorDetailRoute(it))
                                    },
                                )
                            }
                        }
                        composable<CollectionsRoute> {
                            LegacyLibraryAlias(
                                navController = navController,
                                legacyRoute = CollectionsRoute,
                                section = LibrarySection.Collections,
                            )
                        }
                        composable<FavoritesRoute> {
                            LegacyLibraryAlias(
                                navController = navController,
                                legacyRoute = FavoritesRoute,
                                section = LibrarySection.Favorites,
                            )
                        }
                        composable<FavoritesAllRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<FavoritesAllRoute>()
                            SharedTransitionDestination {
                                FavoritesGridRoute(
                                    category = route.category,
                                    onMovieClick = { movieId ->
                                        navController.navigate(MovieDetailRoute(movieId))
                                    },
                                    onSeriesClick = { seriesId ->
                                        navController.navigate(SeriesDetailRoute(seriesId))
                                    },
                                    onActorClick = { actorId ->
                                        navController.navigate(ActorDetailRoute(actorId))
                                    },
                                    onBackClick = { navController.popBackStack() },
                                )
                            }
                        }
                        composable<ProfileRoute> {
                            ProfileScreen(
                                onSettingsClick = { navController.navigate(SettingsRoute) },
                                onAccountClick = { navController.navigate(SettingsAccountRoute) },
                                onThemeClick = { navController.navigate(SettingsThemeRoute) },
                                onLanguageClick = { navController.navigate(SettingsLanguageRoute) },
                                onStorageClick = { navController.navigate(SettingsStorageRoute) },
                                onHelpClick = { navController.navigate(SettingsHelpRoute) },
                                onAboutClick = { navController.navigate(SettingsAboutRoute) },
                            )
                        }
                        composable<WatchlistAllRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<WatchlistAllRoute>()
                            SharedTransitionDestination {
                                WatchlistGridRoute(
                                    category = route.category,
                                    onMovieClick = { movieId ->
                                        navController.navigate(MovieDetailRoute(movieId))
                                    },
                                    onSeriesClick = { seriesId ->
                                        navController.navigate(SeriesDetailRoute(seriesId))
                                    },
                                    onBackClick = { navController.popBackStack() },
                                )
                            }
                        }
                        composable<CollectionDetailRoute> { entry ->
                            val route = entry.toRoute<CollectionDetailRoute>()
                            SharedTransitionDestination {
                                CollectionDetailScreen(
                                    collectionId = route.collectionId,
                                    onBackClick = { navController.popBackStack() },
                                    onCollectionDeleted = { message ->
                                        if (navController.popBackStack()) {
                                            snackbarScope.launch {
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    },
                                    onMovieClick = { movieId ->
                                        navController.navigate(MovieDetailRoute(movieId))
                                    },
                                    onSeriesClick = { seriesId ->
                                        navController.navigate(SeriesDetailRoute(seriesId))
                                    },
                                )
                            }
                        }
                        composable<MovieDetailRoute> { entry ->
                            val route = entry.toRoute<MovieDetailRoute>()
                            SharedTransitionDestination {
                                MovieDetailScreen(
                                    movieId = route.movieId,
                                    onBackClick = { navController.popBackStack() },
                                    onMovieClick = { id ->
                                        navController.navigate(MovieDetailRoute(id))
                                    },
                                    onActorClick = { id ->
                                        navController.navigate(ActorDetailRoute(id))
                                    },
                                    onOpenImageGallery = { id ->
                                        navController.navigate(ImageGalleryRoute("movie", id))
                                    },
                                )
                            }
                        }
                        composable<BrowseAllRoute> { entry ->
                            val route = entry.toRoute<BrowseAllRoute>()
                            SharedTransitionDestination {
                                BrowseAllScreenRoute(
                                    category = route.category,
                                    onMovieClick = { movieId ->
                                        navController.navigate(MovieDetailRoute(movieId))
                                    },
                                    onSeriesClick = { seriesId ->
                                        navController.navigate(SeriesDetailRoute(seriesId))
                                    },
                                    onBackClick = { navController.popBackStack() },
                                )
                            }
                        }
                        composable<UpcomingRoute> {
                            SharedTransitionDestination {
                                UpcomingCalendarScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onMovieClick = { movieId ->
                                        navController.navigate(MovieDetailRoute(movieId))
                                    },
                                )
                            }
                        }
                        composable<SeriesDetailRoute> { entry ->
                            val route = entry.toRoute<SeriesDetailRoute>()
                            SharedTransitionDestination {
                                SeriesDetailScreen(
                                    seriesId = route.seriesId,
                                    onBackClick = { navController.popBackStack() },
                                    onSeriesClick = { seriesId ->
                                        navController.navigate(SeriesDetailRoute(seriesId))
                                    },
                                    onActorClick = { actorId ->
                                        navController.navigate(ActorDetailRoute(actorId))
                                    },
                                    onOpenImageGallery = { id ->
                                        navController.navigate(ImageGalleryRoute("tv", id))
                                    },
                                )
                            }
                        }
                        composable<ImageGalleryRoute> { entry ->
                            val route = entry.toRoute<ImageGalleryRoute>()
                            ImageGalleryScreen(
                                kind = route.kind,
                                id = route.id,
                                onBackClick = { navController.popBackStack() },
                            )
                        }
                        composable<ActorDetailRoute> { entry ->
                            val route = entry.toRoute<ActorDetailRoute>()
                            SharedTransitionDestination {
                                ActorDetailScreen(
                                    actorId = route.actorId,
                                    onBackClick = { navController.popBackStack() },
                                    onMovieClick = { movieId ->
                                        navController.navigate(MovieDetailRoute(movieId))
                                    },
                                    onSeriesClick = { seriesId ->
                                        navController.navigate(SeriesDetailRoute(seriesId))
                                    },
                                    onOpenImageGallery = { id ->
                                        navController.navigate(ImageGalleryRoute("person", id))
                                    },
                                )
                            }
                        }
                        composable<SettingsRoute> {
                            SettingsHomeScreen(
                                onBackClick = { navController.popBackStack() },
                                onAccountClick = { navController.navigate(SettingsAccountRoute) },
                                onCatalogClick = { navController.navigate(SettingsCatalogRoute) },
                                onThemeClick = { navController.navigate(SettingsThemeRoute) },
                                onLanguageClick = { navController.navigate(SettingsLanguageRoute) },
                                onStorageClick = { navController.navigate(SettingsStorageRoute) },
                                onHelpClick = { navController.navigate(SettingsHelpRoute) },
                                onAboutClick = { navController.navigate(SettingsAboutRoute) },
                            )
                        }
                        composable<SettingsAccountRoute> {
                            AccountScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable<SettingsCatalogRoute> {
                            CatalogSettingsScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable<SettingsThemeRoute> {
                            ThemeScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable<SettingsLanguageRoute> {
                            LanguageScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable<SettingsStorageRoute> {
                            StorageScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable<SettingsHelpRoute> {
                            HelpScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable<SettingsAboutRoute> {
                            AboutScreen(
                                versionName = AppConfig.VERSION_NAME,
                                onBackClick = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun AnimatedContentScope.SharedTransitionDestination(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalMovieAppAnimatedVisibilityScope provides this,
        content = content,
    )
}

@Composable
internal fun NavigationSuiteLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 2,
        softWrap = true,
        textAlign = TextAlign.Center,
    )
}

@Composable
internal fun LegacyLibraryAlias(
    navController: NavController,
    legacyRoute: Any,
    section: LibrarySection,
) {
    LaunchedEffect(legacyRoute) {
        navController.replaceLegacyLibraryRoute(legacyRoute, section)
    }
}

internal fun NavController.replaceLegacyLibraryRoute(legacyRoute: Any, section: LibrarySection) {
    navigate(LibraryRoute(section.name)) {
        popUpTo(legacyRoute) { inclusive = true }
    }
}

data class BottomNavItem(
    val routeClass: KClass<*>,
    val titleRes: StringResource,
    val icon: ImageVector,
)

