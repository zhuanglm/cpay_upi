package upisdk.models

data class RequestTransaction(
    var reference: String,
    var amount: Int,
    var currency: String,
    val country: String,
    val auto_capture: Boolean
)
