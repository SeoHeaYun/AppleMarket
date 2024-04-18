package data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemData (
    val id: Long,
    val title: String,
    val image : Int,
    val address: String,
    val price: Int,
    val chat: Int,
    var heart: Int,
    val sellerName: String,
    val itemDetail: String,
    var isLiked: Boolean,
) : Parcelable
