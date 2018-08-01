package software.uniqore.codesample.support

import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.model.Photo

class PhotoLists {
    companion object {
        fun cached() = listOf(Photo("cached", "cachedAuthor", LocalDateTime.now()))
        fun remote() = listOf(Photo("remote", "remoteAuthor", LocalDateTime.now()))
    }
}