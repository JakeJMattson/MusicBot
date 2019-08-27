package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.data.Channels
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.entities.Role

@Service
class AudioPlayerService(channels: Channels) {
    var songQueue: MutableMap<String, MutableList<Song>> = mutableMapOf()
    var playerManager: MutableMap<String, AudioPlayerManager> = mutableMapOf()
    var player: MutableMap<String, AudioPlayer> = mutableMapOf()
    var audioEventService: MutableMap<String, AudioEventService> = mutableMapOf()

    init {
        for (i in channels.channelGroups) {
            songQueue[i.key] = ArrayList()
            playerManager[i.key] = DefaultAudioPlayerManager()

            AudioSourceManagers.registerRemoteSources(playerManager[i.key])
            AudioSourceManagers.registerLocalSource(playerManager[i.key])

            player[i.key] = playerManager[i.key]!!.createPlayer()
            player[i.key]!!.volume = 30
            audioEventService[i.key] = AudioEventService(this)
            player[i.key]!!.addListener(audioEventService[i.key])
        }
    }

    data class Song(val track: AudioTrack, val memberID: String)

    fun queueAdd(guildID: String, song: Song) {
        if (!player[guildID]!!.startTrack(song.track, true)) {
            songQueue[guildID]!!.add(song)
        }
    }

    fun clearByMember(guildID: String, memberID: String){
        for (i in songQueue[guildID]!!){
            if(i.memberID == memberID){
                songQueue[guildID]!!.remove(i)
            }
        }
    }

    fun startNextTrack(guildID: String, noInterrupt: Boolean) {
        val next = songQueue[guildID]!!.firstOrNull()

        if (next != null) {
            player[guildID]!!.startTrack(next.track, noInterrupt)
            songQueue[guildID]!!.removeAt(0)
            //currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
        }else{
            player[guildID]!!.stopTrack()
        }
    }
}