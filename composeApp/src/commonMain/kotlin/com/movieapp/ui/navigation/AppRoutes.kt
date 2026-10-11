package com.movieapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object HomeRoute

@Serializable data object SearchRoute

@Serializable
enum class LibrarySection {
    Watchlist,
    Collections,
    Favorites,
}

@Serializable data class LibraryRoute(val section: String = LibrarySection.Watchlist.name)

@Serializable data object CollectionsRoute

@Serializable data object FavoritesRoute

@Serializable data object ProfileRoute

@Serializable data object UpcomingRoute

@Serializable data object SettingsRoute

@Serializable data object SettingsAccountRoute

@Serializable data object SettingsCatalogRoute

@Serializable data object SettingsThemeRoute

@Serializable data object SettingsLanguageRoute

@Serializable data object SettingsStorageRoute

@Serializable data object SettingsHelpRoute

@Serializable data object SettingsAboutRoute

@Serializable data class MovieDetailRoute(val movieId: Int)

@Serializable data class SeriesDetailRoute(val seriesId: Int)

@Serializable data class ActorDetailRoute(val actorId: Int)

@Serializable data class BrowseAllRoute(val category: String)

@Serializable data class FavoritesAllRoute(val category: String)

@Serializable data class WatchlistAllRoute(val category: String)

@Serializable data class CollectionDetailRoute(val collectionId: String)

@Serializable data class ImageGalleryRoute(val kind: String, val id: Int)

internal fun CollectionsRoute.toLibraryRoute() = LibraryRoute(LibrarySection.Collections.name)

internal fun FavoritesRoute.toLibraryRoute() = LibraryRoute(LibrarySection.Favorites.name)

