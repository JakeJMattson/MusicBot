package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.extensions.toTimeString
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.entities.*
import java.awt.Color

//TODO move these to a config
val playerOnImage = listOf(
    "https://i.imgur.com/DzVDu0c.gif",
    "https://i.imgur.com/0W8ZoUs.gif",
    "https://i.imgur.com/EZm2asN.gif"
)

val playerOffImage = listOf(
    "https://i.imgur.com/S7A1t6W.gif",
    "https://i.imgur.com/MuPScTC.gif",
    "https://i.imgur.com/izVWqaM.gif"
)

//TODO add an embed display limit - it doesn't error out, but it won't show them all
@Service
class EmbedService(private val discord: Discord) {
    fun trackDisplay(guild: Guild, player: AudioPlayerService): MessageEmbed {
        val guildAudio = player.guildAudioMap[guild.id]
        val track = guildAudio?.songQueue?.firstOrNull()?.track ?: return noSong()
        val isPlaying = !guildAudio.player.isPaused

        return embed {
            val songList = guildAudio.songQueue

            if (songList.isNotEmpty()) {
                color = if (isPlaying) Color.CYAN else Color.RED
                thumbnail = if (isPlaying) playerOnImage.random() else playerOffImage.random()

                val nextSize = songList.size - 1
                var midSize = 3
                var maxSize = 5

                if (nextSize >= 5) {
                    midSize = nextSize / 2
                    maxSize = nextSize
                }

                songList.forEachIndexed { index, song ->
                    when (index) {
                        0 -> addField("Now Playing:",
                            "- **Song**: [${track.info.title}](${track.info.uri})\n" +
                                "- **Artist**: ${track.info.author}\n" +
                                "- **Duration**: ${track.duration.toTimeString()}\n" +
                                "- **Queued by**: ${guild.getMemberById(songList.first.memberID)?.asMention}")
                        1 -> {
                            addField("", "__**Next Songs:**__")

                            addField("",
                                "$index) [${song.track.info.title}](${song.track.info.uri})\n" +
                                    "- **Artist**: ${song.track.info.author}\n" +
                                    "- **Duration**: ${song.track.duration.toTimeString()}\n" +
                                    "- **Queued by**: ${guild.getMemberById(song.memberID)?.asMention}")
                        }
                        2, (maxSize - 1), maxSize -> {
                            addField("",
                                "$index) [${song.track.info.title}](${song.track.info.uri})\n" +
                                    "- **Artist**: ${song.track.info.author}\n" +
                                    "- **Duration**: ${song.track.duration.toTimeString()}\n" +
                                    "- **Queued by**: ${guild.getMemberById(song.memberID)?.asMention}")
                        }
                        midSize -> {
                            addField("", "**:**\n**:**\n**:**\n**:**")
                        }
                    }
                }
            }
        }
    }

    fun addSong(guild: Guild, memberID: String, track: AudioTrack) = embed {
        addField("**Added a new song:**",
            "- **Song**: [${track.info.title}](${track.info.uri})\n" +
                "- **Artist**: ${track.info.author}\n" +
                "- **Duration**: ${track.duration.toTimeString()}\n" +
                "- **Queued by**: ${guild.getMemberById(memberID)?.asMention}")
        color = Color.green
    }

    fun noSong() = embed {
        addField("There are no more songs currently in the queue.",
            "If you would like to add a song type:\n${discord.configuration.prefix}Play <Song URL>")
        color = Color.red
    }
}