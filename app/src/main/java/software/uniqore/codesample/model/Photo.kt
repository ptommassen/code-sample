package software.uniqore.codesample.model

import android.databinding.BaseObservable
import org.threeten.bp.LocalDateTime


data class Photo(val url: String, val author: String, val date: LocalDateTime) : BaseObservable() {
}

