package com.optv.player

data class UserInfo(
  val username: String? = null,
  val exp_date: String? = null
)

data class AuthResponse(
  val user_info: UserInfo? = null
)

data class StreamItem(
  val stream_id: Long = 0,
  val name: String? = null,
  val stream_icon: String? = null
)

data class VodItem(
  val stream_id: Long = 0,
  val name: String? = null,
  val stream_icon: String? = null
)

data class SeriesItem(
  val series_id: Long = 0,
  val name: String? = null,
  val cover: String? = null
)