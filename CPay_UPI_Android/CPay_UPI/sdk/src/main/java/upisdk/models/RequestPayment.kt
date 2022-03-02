package upisdk.models

data class RequestPayment(
    var method: String,
    var indicator: String,
    var request_token: Boolean,
    var token: String,
    var timeout: Long,
    var client: ArrayList<String>
)
