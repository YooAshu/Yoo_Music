package com.example.yoomusic.data

class LyricsRepository(
    private val api: LyricsApi = RetrofitInstance.api
) {

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

            val syncedMatch = results.firstOrNull {
                !it.instrumental && !it.syncedLyrics.isNullOrBlank()
            }
            if (syncedMatch != null) {
                return Result.success(syncedMatch)
            }

            val plainMatch = results.firstOrNull {
                !it.instrumental && !it.plainLyrics.isNullOrBlank()
            }
            if (plainMatch != null) {
                return Result.success(plainMatch)
            }

            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchLyricsMultiple(query: String): Result<List<LrcLibResponse>> {
        return try {
            val cleanedQuery = LyricsSearchHelper.cleanSongTitle(query)
            val response = api.searchLyrics(cleanedQuery)

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code()}"))
            }

            val results = response.body().orEmpty()
                .filter { !it.instrumental && (!it.syncedLyrics.isNullOrBlank() || !it.plainLyrics.isNullOrBlank()) }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
