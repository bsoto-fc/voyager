package mx.unam.ciencias.myp.miner

import java.io.File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.audio.AudioFileIO

class Miner(val path: String) {
    fun recursiveMine() {
        val dir = File(path)
        if(dir.exists() && dir.isDirectory) {
            File(path).walkTopDown()
                .filter {
                    it.extension.equals("mp3", ignoreCase = true)
                }
                .forEach {
                    val audioFile = AudioFileIO.read(it)
                    val tag = audioFile.tag
                    
                    println("Title:  ${tag.getFirst(FieldKey.TITLE)}")
                    println("Artist: ${tag.getFirst(FieldKey.ARTIST)}")
                    println("Album:  ${tag.getFirst(FieldKey.ALBUM)}")
                    println("Track:  ${tag.getFirst(FieldKey.TRACK)}")
                }
        }
    }
}
