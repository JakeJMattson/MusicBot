package services

import data.Configuration
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord

@Service
class PrefixService(private val configuration: Configuration, private val discord: Discord) {
    init {
        setPrefix(configuration.prefix)
    }

    fun setPrefix(prefix: String) {
        configuration.prefix = prefix
        discord.configuration.prefix = prefix
    }
}