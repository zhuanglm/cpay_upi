package upisdk.models

import org.json.JSONObject

data class RespondChargePaymentClient(val client: String, val format: String, val method: String,
                                      val content: String)
