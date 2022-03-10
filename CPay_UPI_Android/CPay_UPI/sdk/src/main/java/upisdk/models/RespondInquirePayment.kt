package upisdk.models

import org.json.JSONObject

data class RespondInquirePayment(val method: String, val token: String, val type: String, val data: JSONObject)
