package jp.techacademy.shunsuke.kino.qa_app

import java.io.Serializable
import java.util.ArrayList

class FavoriteQuestion(
    val title: String,
    val body: String,
    val name: String,
    //val uid: Int,
    val questionUid: String,
    val genre: String,
    bytes: ByteArray,
    val answers: ArrayList<Answer>
) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}