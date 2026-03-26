package com.encer.offlinesplitwise.data.update

import com.encer.offlinesplitwise.BuildConfig
import com.encer.offlinesplitwise.data.remote.model.HealthCheckResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateCheckerTest {
    @Test
    fun `forces hard update below min supported version`() {
        val state = resolveAppUpdateState(
            currentVersionCode = 10,
            payload = HealthCheckResponse(
                status = "ok",
                minSupportedVersionCode = 12,
                latestVersionCode = 18,
                updateMode = "soft",
                storeUrl = "https://cafebazaar.ir/app/com.encer.offlinesplitwise",
            )
        )

        assertEquals(AppUpdateMode.HARD, state.mode)
        assertTrue(state.isVisible)
    }

    @Test
    fun `shows soft update when below latest version and mode is soft`() {
        val state = resolveAppUpdateState(
            currentVersionCode = 14,
            payload = HealthCheckResponse(
                status = "ok",
                minSupportedVersionCode = 12,
                latestVersionCode = 18,
                updateMode = "soft",
                storeUrl = "https://cafebazaar.ir/app/com.encer.offlinesplitwise",
            )
        )

        assertEquals(AppUpdateMode.SOFT, state.mode)
        assertTrue(state.isVisible)
    }

    @Test
    fun `stays hidden when update mode is none or version is current`() {
        val state = resolveAppUpdateState(
            currentVersionCode = 18,
            payload = HealthCheckResponse(
                status = "ok",
                latestVersionCode = 18,
                updateMode = "none",
                storeUrl = "https://cafebazaar.ir/app/com.encer.offlinesplitwise",
            )
        )

        assertEquals(AppUpdateMode.NONE, state.mode)
        assertFalse(state.isVisible)
    }

    @Test
    fun `falls back to flavor store url when backend response omits store url`() {
        val state = resolveAppUpdateState(
            currentVersionCode = 14,
            payload = HealthCheckResponse(
                status = "ok",
                latestVersionCode = 18,
                updateMode = "soft",
                storeUrl = null,
            )
        )

        assertEquals(AppUpdateMode.SOFT, state.mode)
        assertEquals(BuildConfig.DEFAULT_STORE_URL, state.storeUrl)
        assertTrue(state.isVisible)
    }
}
