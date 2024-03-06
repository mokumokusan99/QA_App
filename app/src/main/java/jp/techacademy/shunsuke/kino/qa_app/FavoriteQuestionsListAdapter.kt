package jp.techacademy.shunsuke.kino.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.techacademy.shunsuke.kino.qa_app.databinding.ListFavoritequestionsBinding

class FavoriteQuestionsListAdapter (context: Context) : BaseAdapter() {
    private var layoutInflater: LayoutInflater
    private var favoritequestionArrayList = ArrayList<FavoriteQuestion>()

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return favoritequestionArrayList.size
    }

    override fun getItem(position: Int): Any {
        return favoritequestionArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ListFavoritequestionsBinding
        val view: View

        if (convertView == null) {
            binding = ListFavoritequestionsBinding.inflate(layoutInflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ListFavoritequestionsBinding
        }

        binding.titleTextView.text = favoritequestionArrayList[position].title
        binding.nameTextView.text = favoritequestionArrayList[position].name
        binding.resTextView.text = favoritequestionArrayList[position].answers.size.toString()

        val bytes = favoritequestionArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            binding.imageView.setImageBitmap(image)
        }

        return view
    }

    fun setFavoriteQuestionArrayList(favoritequestionArrayList: ArrayList<FavoriteQuestion>) {
        this.favoritequestionArrayList = favoritequestionArrayList
    }
}