/**
 * TMDB HTTP usage inventory for `TmdbApiService`.
 *
 * **Production (called from data layer modules, paging, mediators):**
 *
 * Movies: getPopularMovies, getTopRatedMovies, getUpcomingMovies, getNowPlayingMovies,
 * getMovieDetails (append_to_response=videos), searchMovies, discoverMovies, getTrendingMovies,
 * getSimilarMovies, getMovieRecommendations, getMovieWatchProviders, getMovieCredits,
 * getMovieReviews, getMovieKeywords, getMovieExternalIds, getMovieImages, getMovieGenres.
 *
 * TV: getTvSeriesDetails (videos append), searchTvSeries, discoverTvSeries, getSimilarTvSeries,
 * getTvSeriesRecommendations, getPopularTvSeries, getTopRatedTvSeries, getAiringTodayTvSeries,
 * getOnTheAirTvSeries, getTrendingTvSeries, getTvSeriesWatchProviders, getTvSeriesCredits,
 * getTvSeriesReviews, getTvSeriesKeywords, getTvSeriesExternalIds, getTvSeriesImages, getTvGenres.
 * Provider catalog: TmdbWatchProviderApi.getMovieWatchProviderCatalog,
 * TmdbWatchProviderApi.getTvWatchProviderCatalog.
 *
 * People: getActorDetails, searchActors, getPopularActors, getActorMovieCredits, getActorTvCredits,
 * getActorImages, getActorTaggedImages (merged in ActorRepository.getActorImages for gallery).
 *
 * Search (blended list): multiSearch (MovieRepository.multiSearchPaged / feature search "All" tab);
 * Search tab idle: getTrendingMovies + getTrendingTvSeries page snapshot (SearchViewModel
 * spotlight). API `language` query: `PreferencesRepository.tmdbLanguage` (see `TmdbLocaleMapper`
 * when catalog language is still the default `en-US`).
 *
 * **Declared but uncalled at data layer** (use when product needs them):
 *
 * Movie (some read-only / optional paths): getMovieVideos as standalone (videos also via append).
 * TV: getTvSeriesVideos standalone. Person: getActorExternalIds.
 */
@Suppress("unused") private object TmdbApiInventory
