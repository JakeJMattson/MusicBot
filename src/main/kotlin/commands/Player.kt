package commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.arguments.UrlArg
import me.aberrantfox.kjdautils.internal.command.arguments.VoiceChannelArg
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import services.AudioPlayerSendHandler
import services.ManagerService

@CommandSet("Player")
fun playerCommands(plugin: ManagerService) = commands {
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String
            var vc = it.author.toMember(it.guild!!)?.voiceState?.channel

            if (vc == null) {
                vc = it.guild!!.voiceChannels[0]
            }

            val am: AudioManager = it.guild!!.audioManager
            am.sendingHandler = AudioPlayerSendHandler(plugin.player)
            am.openAudioConnection(vc)

            plugin.playerManager.loadItem(url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    it.respond("Successfully loaded the track.")
                    plugin.handler.queue(track)
                    plugin.player.playTrack(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    for (track in playlist.tracks) {
                        plugin.handler.queue(track)
                    }
                }

                override fun noMatches() = it.respond("No matching song found")

                override fun loadFailed(throwable: FriendlyException) = it.respond("Error, could not load track.")
            })
        }
    }

    command("Move") {
        description = "Move bot to the current voice channel or to a specified voice channel via ID."
        requiresGuild = true
        expect(arg(VoiceChannelArg, true) { it.author.toMember(it.guild!!)?.voiceState?.channel ?: Unit })
        execute {
            if (it.args.component1() is Unit) {
                return@execute it.respond("Sorry, you need to either be in a channel or specify a valid channel ID")
            }

            val channel = it.args.component1() as VoiceChannel
            val manager = it.guild!!.audioManager
            manager.sendingHandler = AudioPlayerSendHandler(plugin.player)
            manager.openAudioConnection(channel)
        }
    }
}