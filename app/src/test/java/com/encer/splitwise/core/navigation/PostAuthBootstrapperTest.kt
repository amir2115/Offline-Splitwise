package com.encer.splitwise.core.navigation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PostAuthBootstrapperTest {
    @Test
    fun `runs health update and sync in order exactly once`() = runTest {
        val calls = mutableListOf<String>()
        val bootstrapper = PostAuthBootstrapper(
            refreshReachability = { calls += "health" },
            refreshUpdatePolicy = { calls += "update" },
            restoreAndSyncSession = { calls += "sync" },
        )

        bootstrapper.run()

        assertEquals(listOf("health", "update", "sync"), calls)
    }
}
