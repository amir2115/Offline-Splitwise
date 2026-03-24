package com.encer.offlinesplitwise.data.remote.model

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteModelsSerializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun decodesHealthCheckResponse() {
        val payload = """{"status":"ok"}"""

        val result = json.decodeFromString(HealthCheckResponse.serializer(), payload)

        assertEquals("ok", result.status)
    }

    @Test
    fun decodesGroupInvitesResponseFromBackendSchema() {
        val payload = """
            [
              {
                "id": "invite-1",
                "group_id": "group-1",
                "group_name": "Trip",
                "member_id": "member-1",
                "username": "alice",
                "inviter_user_id": "user-1",
                "inviter_username": "bob",
                "invitee_user_id": "user-2",
                "invitee_username": "alice",
                "status": "PENDING",
                "responded_at": null,
                "created_at": "2026-03-24T12:00:00Z",
                "updated_at": "2026-03-24T12:00:00Z"
              }
            ]
        """.trimIndent()

        val result = json.decodeFromString(ListSerializer(RemoteGroupInvite.serializer()), payload)

        assertEquals(1, result.size)
        assertEquals("invite-1", result.first().id)
        assertEquals("member-1", result.first().memberId)
        assertEquals("alice", result.first().username)
        assertEquals("PENDING", result.first().status)
        assertNull(result.first().respondedAt)
    }
}
