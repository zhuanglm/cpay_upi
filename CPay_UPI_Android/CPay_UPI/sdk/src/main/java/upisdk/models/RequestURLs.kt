package upisdk.models

data class RequestURLs(
    var ipn: String,
    var mobile: String?,
    var success: String?,
    var cancel: String?,
    var fail: String?
)
