package jp.techacademy.shunsuke.kino.qa_app

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.shunsuke.kino.qa_app.databinding.ActivityQuestionDetailBinding

class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionDetailBinding

    private lateinit var question: Question
    private lateinit var adapter: QuestionDetailListAdapter
    private lateinit var answerRef: DatabaseReference

    private lateinit var favoritesRef: DatabaseReference


    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in question.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            question.answers.add(answer)
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        // API33以上でgetSerializableExtra(key)が非推奨となったため処理を分岐
        @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATED_SYNTAX_WITH_DEFINITELY_NOT_NULL")
        question = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("question", Question::class.java)!!
        else
            intent.getSerializableExtra("question") as? Question!!

        title = question.title

        // ListViewの準備
        adapter = QuestionDetailListAdapter(this, question)
        binding.listView.adapter = adapter
        // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            var favoriteImageView = findViewById<ImageView>(R.id.favoriteImageView)

            if (user == null) {
            // ログインしていなければお気に入りボタンを表示しない
                favoriteImageView.visibility = View.GONE
        } else {
                // ログインしていればお気に入りボタンを表示させる
                favoriteImageView.visibility = View.VISIBLE
            }

        adapter.notifyDataSetChanged()

        binding.fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", question)
                startActivity(intent)
                // --- ここまで ---
            }
        }
// お気に入りアイコンの初期表示を設定
       // val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val questionId = question.questionUid

            favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites")
            favoritesRef.child(userId).child(questionId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // 質問がお気に入りに登録されている場合
                        binding.favoriteImageView.setImageResource(R.drawable.ic_star)
                    } else {
                        // 質問がお気に入りに登録されていない場合
                        binding.favoriteImageView.setImageResource(R.drawable.ic_star_border)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // データベースエラーの処理
                }
            })
        }
        // お気に入りボタンが押されたときの処理
        binding.favoriteImageView.setOnClickListener {
            // ログイン済みのユーザ情報を取得
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていない場合は何もしない
            if (user == null) {
                return@setOnClickListener
            }

            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites")
            val userId = user.uid
            val questionId = question.questionUid

            favoritesRef.child(userId).child(questionId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        // Question is already a favorite, remove it
                        favoritesRef.child(userId).child(questionId).removeValue()
                        binding.favoriteImageView.setImageResource(R.drawable.ic_star_border)
                    } else {
                        // Question is not a favorite, add it
                        favoritesRef.child(userId).child(questionId).setValue(true)
                        binding.favoriteImageView.setImageResource(R.drawable.ic_star)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }




        val dataBaseReference = FirebaseDatabase.getInstance().reference
        answerRef = dataBaseReference.child(ContentsPATH).child(question.genre.toString())
            .child(question.questionUid).child(AnswersPATH)
        answerRef.addChildEventListener(eventListener)
    }
}