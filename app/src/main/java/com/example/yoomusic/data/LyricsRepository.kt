package com.example.yoomusic.data


class LyricsRepository(
    private val api: LyricsApi = RetrofitInstance.api
) {

    /**
     * Search LRCLib for the song title.
     * Returns:
     *  - First match with syncedLyrics
     *  - OR match with plainLyrics
     *  - OR null if no result
     */
    suspend fun searchLyrics(songTitle: String): Result<LrcLibResponse?> {
        return try {

            val cleanedTitle = LyricsSearchHelper.cleanSongTitle(songTitle)

            val response = api.searchLyrics(cleanedTitle)

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code()}"))
            }

            val results = response.body().orEmpty()

            if (results.isEmpty()) {
                return Result.success(null)
            }

            // 1. Prefer synced lyrics
            val syncedMatch = results.firstOrNull {
                !it.instrumental && !it.syncedLyrics.isNullOrBlank()
            }
            if (syncedMatch != null) {
                return Result.success(syncedMatch)
            }

            // 2. Otherwise, use plain lyrics
            val plainMatch = results.firstOrNull {
                !it.instrumental && !it.plainLyrics.isNullOrBlank()
            }
            if (plainMatch != null) {
                return Result.success(plainMatch)
            }

            // 3. Instrumental or no lyrics → treat as no lyrics
            Result.success(null)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
