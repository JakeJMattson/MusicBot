package io.github.cferg.musicbot.utility

import io.github.cferg.musicbot.extensions.*
import me.aberrantfox.kjdautils.api.dsl.embed
import net.dv8tion.jda.api.entities.*
import java.awt.Color

//TODO move these to a config
private val playerOnImage = listOf(
    "https://i.imgur.com/DzVDu0c.gif",
    "https://i.imgur.com/0W8ZoUs.gif",
    "https://i.imgur.com/EZm2asN.gif"
)

private val playerOffImage = listOf(
    "https://i.imgur.com/S7A1t6W.gif",
    "https://i.imgur.com/MuPScTC.gif",
    "https://i.imgur.com/izVWqaM.gif"
)

fun currentTrackEmbed(guild: Guild): MessageEmbed {
    val currentSong = guild.fetchNextSong()
    val isPlaying = guild.isTrackPlaying()

    if (currentSong == null){
        return displayNoSongEmbed()
    }

    return embed {
        color = if (isPlaying) Color.CYAN else Color.RED
        thumbnail = if (isPlaying) playerOnImage.random() else playerOffImage.random()

        addField("Now Playing:", formatSong(currentSong, guild))
    }
}

fun displayTrackEmbed(guild: Guild): MessageEmbed {
    val songList = guild.fetchUpcomingSongs()
    val isPlaying = guild.isTrackPlaying()

    if (songList.isEmpty())
        return displayNoSongEmbed()

    return embed {
        color = if (isPlaying) Color.CYAN else Color.RED
        thumbnail = if (isPlaying) playerOnImage.random() else playerOffImage.random()
        val songSize = songList.size

        songList.forEachIndexed { index, song ->
            when (index) {
                0 -> addField("Now Playing:", formatSong(song, guild))
                1 -> {
                    addField("", "__**Next Songs:**__")
                    addField("", formatSong(song, guild, "$index)"))
                }
                2,3,4 -> addField("", formatSong(song, guild, "$index)"))
                5 -> addField("", "${formatSong(song, guild, "$index)")}${remaining(songSize)}")
            }
        }
    }
}

fun displayNoSongEmbed() = embed {
    addField("There are no more songs currently in the queue. ",
        "If you would like to add a song, use the `Play` command.")
    color = Color.red
}

private fun remaining(songSize: Int) = when {
    songSize - 6 > 1 -> "\n\n...and **${songSize - 6}** others."
    songSize - 6 == 1 -> "\n\n...and **1** other."
    else -> ""
}

private fun formatSong(song: Song, guild: Guild, header: String = "- **Song**:"): String {
    val track = song.track
    val trackInfo = track.info

    return "$header [${trackInfo.title}](${trackInfo.uri})\n" +
        "- **Artist**: ${trackInfo.author}\n" +
        "- **Duration**: ${track.duration.toTimeString()}\n" +
        "- **Queued by**: ${guild.getMemberById(song.memberID)?.asMention}"
}