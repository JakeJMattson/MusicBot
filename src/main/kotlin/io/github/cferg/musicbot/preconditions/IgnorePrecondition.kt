package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.services.ConversationService
import net.dv8tion.jda.api.entities.TextChannel

@Precondition(1)
fun isIgnored(config: Configuration) = exit@{ event: CommandEvent ->
    if (event.channel !is TextChannel) return@exit Fail("**Failure:** This command must be executed in a text channel.")
    val guild = event.guild ?: return@exit Fail("**Failure:** This command must be ran in a guild.")
    val eventMember = event.author.toMember(guild)!!
    val guildConfig = config.guildConfigurations[event.guild!!.id] ?:
    return@exit Pass

    if (!guildConfig.ignoreList.contains(eventMember.id)) return@exit Pass

    return@exit Fail("You are currently blacklisted form using this bot's commands.")
}