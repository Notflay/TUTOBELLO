package com.example.adopmekotlin.activity.activity

//import com.google.firebase.database.ktx.database
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adopmekotlin.R
import com.example.adopmekotlin.activity.entity.Post
import com.example.adopmekotlin.activity.entity.User
import com.example.adopmekotlin.activity.recycler.PostCardAdapter
import com.example.adopmekotlin.activity.recycler.PostCardItemDecoration
import com.example.adopmekotlin.activity.util.UserUtils
import com.example.adopmekotlin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var usersReference: DatabaseReference
    private lateinit var postsReference: DatabaseReference
    private lateinit var user: User
    private val postCardAdapter = PostCardAdapter(mutableListOf(), null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        usersReference = Firebase.database.getReference(getString(R.string.users_reference))
        postsReference = Firebase.database.getReference(getString(R.string.posts_reference))
        setupView()
    }

    private fun setupView() {
        val postCardHorizontalSpacing =
            resources.getDimensionPixelSize(R.dimen.app_horizontal_spacing)
        val postCardBottomSpacing =
            resources.getDimensionPixelSize(R.dimen.card_post_bottom_spacing)

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_home -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_item_my_posts -> {
                    navigateToSearchResult(SearchResultActivity.SearchFor.MY_POSTS)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    false
                }
                R.id.menu_item_logout -> {
                    UserUtils.removeUserFromSharedPreferences(this)
                    auth.signOut()
                    navigateToSignIn()
                    false
                }
                else -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    false
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    //Traemos los GetPosts()
                    val posts = getPosts()
                    //Si el Post noe s Nulo
                    posts?.let {
                        //Asignamos el Nuevo Post al PostCardAdapter
                        postCardAdapter.setItems(posts.toMutableList())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        binding.searchPetsInputLayout.setEndIconOnClickListener {
            val search = binding.searchPetsInputEditText.text.toString()
            if (!search.isNullOrBlank()) {
                navigateToSearchResult(SearchResultActivity.SearchFor.CUSTOM, search)
            }
        }

        binding.searchPetsInputEditText.addTextChangedListener {
            if (!it.toString().isNullOrBlank()) {
                binding.searchPetsInputLayout.endIconDrawable =
                    getDrawable(R.drawable.ic_search_pets)
            } else {
                binding.searchPetsInputLayout.endIconDrawable = null
            }
        }

        binding.searchForDogsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.DOGS)
        }

        binding.searchForCatsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.CATS)
        }

        binding.searchForRabbitsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.RABBITS)
        }

        binding.searchForHamsterBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.HAMSTERS)
        }

        binding.searchForBirdsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.BIRDS)
        }

        binding.cardPostRecyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(
                PostCardItemDecoration(
                    postCardHorizontalSpacing,
                    postCardBottomSpacing
                )
            )
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = postCardAdapter
        }

        binding.createPostFab.setOnClickListener {
            navigateToCreatePost()
        }

        usersReference.child(auth.currentUser!!.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && result.exists()) {
                        Log.d(TAG, "Retrieving user data: success")
                        user = result.getValue(User::class.java)!!
                        updateUserInfo()
                    } else {
                        Log.w(TAG, "User data does not exist")
                    }
                } else {
                    Log.w(TAG, "Retrieving user data: failure", task.exception)
                }
            }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                user = getUserData()
                updateUserInfo()

                val posts = getPosts()
                postCardAdapter.setItems(posts.toMutableList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getPosts(): List<Post> {
        return try {
            postsReference.orderByChild("createdAt").limitToLast(10).get().await()
                .children
                .map { it.getValue(Post::class.java)!! }
                .reversed()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener publicaciones", e)
            emptyList()
        }
    }

    private suspend fun getUserData(): User {
        return try {
            usersReference.child(auth.currentUser!!.uid).get().await().getValue(User::class.java)!!
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener datos del usuario", e)
            User()
        }
    }

    private fun updateUserInfo() {
        val userFullNameTextView = findViewById<TextView>(R.id.user_full_name_text_view)
        val userEmailTextView = findViewById<TextView>(R.id.user_email_text_view)
        userFullNameTextView.text = "${user.firstname} ${user.lastname}"
        userEmailTextView.text = user.email
        binding.welcomeUserTextView.text =
            getString(R.string.main_welcome_user, "${user.firstname}")
    }

    private fun navigateToSearchResult(
        searchFor: SearchResultActivity.SearchFor,
        payload: String? = null
    ) {
        val intent = Intent(this, SearchResultActivity::class.java).apply {
            putExtra("search_for", searchFor.name)
            putExtra("payload", payload?.trim())
        }
        startActivity(intent)
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java).apply {
            putExtra("show_splash_screen", false)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToCreatePost() {
        val intent = Intent(this, CreatePostActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
    }


}