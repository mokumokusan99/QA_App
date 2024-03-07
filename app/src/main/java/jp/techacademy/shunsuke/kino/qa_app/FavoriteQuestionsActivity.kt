package jp.techacademy.shunsuke.kino.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.shunsuke.kino.qa_app.databinding.ActivityFavoriteQuestionsBinding

class FavoriteQuestionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteQuestionsBinding

    private var genre = 0

    private var favoritesRef: DatabaseReference? = null


    // ----- 追加:ここから -----
    private lateinit var databaseReference: DatabaseReference
    private lateinit var favoritequestionArrayList: ArrayList<FavoriteQuestion>
    private lateinit var adapter: FavoriteQuestionsListAdapter //adapterは作り直し

    //private var genreRef: DatabaseReference? = null

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>
            val title = map["title"] as? String ?: ""
            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""
            val questionUid = map["questionUid"] as? String ?: ""
            val imageString = map["image"] as? String ?: ""
            val genre = map["genre"] as? String ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<*, *>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val map1 = answerMap[key] as Map<*, *>
                    val map1Body = map1["body"] as? String ?: ""
                    val map1Name = map1["name"] as? String ?: ""
                    val map1Uid = map1["uid"] as? String ?: ""
                    val map1AnswerUid = key as? String ?: ""
                    val answer = Answer(map1Body, map1Name, map1Uid, map1AnswerUid)
                    answerArrayList.add(answer)
                }
            }

            val favoritequestion = FavoriteQuestion(
                title, body, name, uid,dataSnapshot.key ?: "",
                genre, bytes, answerArrayList)
            favoritequestionArrayList.add(favoritequestion)
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            // 変更があったQuestionを探す
            for (favoritequestion in favoritequestionArrayList) {
                if (dataSnapshot.key.equals(favoritequestion.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    favoritequestion.answers.clear()
                    val answerMap = map["answers"] as Map<*, *>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val map1 = answerMap[key] as Map<*, *>
                            val map1Body = map1["body"] as? String ?: ""
                            val map1Name = map1["name"] as? String ?: ""
                            val map1Uid = map1["uid"] as? String ?: ""
                            val map1AnswerUid = key as? String ?: ""
                            val answer = Answer(map1Body, map1Name, map1Uid, map1AnswerUid)
                            favoritequestion.answers.add(answer)
                        }
                    }


                    adapter.notifyDataSetChanged()
                }
            }
        }


        override fun onChildRemoved(p0: DataSnapshot) {}
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
        override fun onCancelled(p0: DatabaseError) {}
    }
    // ----- 追加:ここまで -----

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        favoritequestionArrayList = ArrayList()

        // ListViewの準備
        adapter = FavoriteQuestionsListAdapter(this)
 //     favoritequestionArrayList = ArrayList()
        binding.content.listView.adapter = adapter

        // Firebaseからデータを取得してリストに追加
        val user = FirebaseAuth.getInstance().currentUser
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
/*
        databaseReference.child("favorites").child(userUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    favoritequestionArrayList.clear() // リストを一度クリアしてからデータを追加する
                    for (snapshot in dataSnapshot.children) {
                        val questionUid = snapshot.key
                        // 質問のUIDを使って質問データを取得
                        databaseReference.child("favorites").child(userUid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(questionDataSnapshot: DataSnapshot) {
                                    val map = questionDataSnapshot.value as Map<*, *>
                                    val title = map["title"] as? String ?: ""
                                    val body = map["body"] as? String ?: ""
                                    val name = map["name"] as? String ?: ""
                                    val questionUid = map["questionUid"] as? String ?: ""
                                    val imageString = map["image"] as? String ?: ""
                                    val genre = map["genre"] as? String ?:""
                                    val bytes =
                                        if (imageString.isNotEmpty()) {
                                            Base64.decode(imageString, Base64.DEFAULT)
                                        } else {
                                            byteArrayOf()
                                        }

                                    val answerArrayList = ArrayList<Answer>()
                                    val answerMap = map["answers"] as Map<*, *>?
                                    if (answerMap != null) {
                                        for (key in answerMap.keys) {
                                            val map1 = answerMap[key] as Map<*, *>
                                            val map1Body = map1["body"] as? String ?: ""
                                            val map1Name = map1["name"] as? String ?: ""
                                            val map1Uid = map1["uid"] as? String ?: ""
                                            val map1AnswerUid = key as? String ?: ""
                                            val answer = Answer(map1Body, map1Name, map1Uid, map1AnswerUid)
                                            answerArrayList.add(answer)
                                        }
                                    }

                                    val favoritequestion = FavoriteQuestion(
                                        title, body, name,dataSnapshot.key ?: "",
                                        genre, bytes, answerArrayList)
                                    favoritequestionArrayList.add(favoritequestion)
                                    adapter.notifyDataSetChanged() // データが更新されたことをAdapterに通知する
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // エラー処理
                                }
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // エラー処理
                }
            })
*/

          favoritequestionArrayList.clear()
          adapter.setFavoriteQuestionArrayList(favoritequestionArrayList)
          binding.content.listView.adapter = adapter

        // リスナーを登録する
        val userRef = databaseReference.child("favorites").child(userUid)
        if (userRef != null) {
            userRef!!.removeEventListener(eventListener)
        }

        userRef!!.addChildEventListener(eventListener)



        // リストビューの項目がクリックされた時の処理
        binding.content.listView.setOnItemClickListener { _, _, position, _ ->
            if (position < favoritequestionArrayList.size) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
                val favoriteQuestion = favoritequestionArrayList[position].toQuestion()
                intent.putExtra("question", favoriteQuestion)
                startActivity(intent)
            } else {
                // インデックスが範囲外の場合は何もしないか、エラーメッセージを表示するなどの適切な処理を行う
                // ここでは何もしない
            }
        }
    }
}
/*
        override fun onResume() {
            super.onResume()
            val navigationView = findViewById<NavigationView>(R.id.nav_view)

            // 1:趣味を既定の選択とする
            if (genre == 0) {
                onNavigationItemSelected(navigationView.menu.getItem(0))
            }
        }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.menu_main, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId

            if (id == R.id.action_settings) {
                val intent = Intent(applicationContext, SettingActivity::class.java)
                startActivity(intent)
                return true
            }

            return super.onOptionsItemSelected(item)
        }

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.nav_hobby -> {
                    binding.content.toolbar.title = getString(R.string.menu_hobby_label)
                    genre = 1
                }
                R.id.nav_life -> {
                    binding.content.toolbar.title = getString(R.string.menu_life_label)
                    genre = 2
                }
                R.id.nav_health -> {
                    binding.content.toolbar.title = getString(R.string.menu_health_label)
                    genre = 3
                }
                R.id.nav_computer -> {
                    binding.content.toolbar.title = getString(R.string.menu_computer_label)
                    genre = 4
                }
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START)
*/
/*
            // ----- 追加:ここから -----
            // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す

            questionArrayList.clear()
            adapter.setQuestionArrayList(questionArrayList)
            binding.content.inner.listView.adapter = adapter

            // 選択したジャンルにリスナーを登録する
            if (genreRef != null) {
                genreRef!!.removeEventListener(eventListener)
            }
            genreRef = databaseReference.child(ContentsPATH).child(genre.toString())
            genreRef!!.addChildEventListener(eventListener)
            // ----- 追加:ここまで -----

            return true
        }
    }

 */