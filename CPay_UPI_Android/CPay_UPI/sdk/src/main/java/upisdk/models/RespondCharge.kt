package upisdk.models

import com.google.gson.annotations.SerializedName

data class RespondCharge(
    @SerializedName("object") val operation: String,
    val charge_token: String,
    val id: String,
    val reference: String,
    val amount: Int,
    val currency: String,
    val time_created: Long,
    val time_captured: Long?,
    val auto_capture: Boolean,
    val status: String,
    val country: String,
    val payment: RespondChargePayment
)
