package upisdk.models

import java.io.Serializable

data class ErrorMessage(var code: String, var message: String, var debug: String?) : Serializable