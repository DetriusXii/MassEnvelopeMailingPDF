package ca.regina.ballroom.models

class StartEmailContext(val hostname: String,
		val portNumber: Int,
		val username: String,
		val password: String,
		val subjectLine: String,
		val subjectBody: String,
		val rbdcAccessDatabaseFile: java.io.File,
		val rbdcEmptyRegistrationForm: java.io.File,
		val extraAttachments: List[java.io.File]
)