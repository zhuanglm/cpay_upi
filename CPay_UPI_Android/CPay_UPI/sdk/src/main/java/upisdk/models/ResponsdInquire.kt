package upisdk.models

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.io.Serializable

data class ResponsdInquire(
    @SerializedName("object") val operation: String,
    val id: String,
    val reference: String,
    val amount: Int,
    val amount_captured: Int?,
    val amount_refunded: Int?,
    val currency: String,
    val time_created: Long,
    val time_canceled: String?,
    val time_captured: Long?,
    val status: String,
    val country: String,
    val expiry: Long?,
    val auto_capture: Boolean?,
    val payment: RespondInquirePayment,
    val exchange: JSONObject,
    val captures: JSONObject,
    val refunds: JSONObject,
    val chargebacks: JSONObject
): Serializable
