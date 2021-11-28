package config

data class ApplicationConfig(val database: Database, val telegram: Telegram)

data class Database(val url: String, val user: String, val password: String)

data class Telegram(val token: String)
