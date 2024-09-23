package no.hiof.groupone.habittracker

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.rpc.context.AttributeContext.Auth

class ViewModel : ViewModel(){

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val authenticationState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = authenticationState

    init {
        checkAuthState()
    }

    fun checkAuthState(){
        val currentUser = auth.currentUser
        if(currentUser!=null) {
            authenticationState.value = AuthState.Authenticated
        } else {
            authenticationState.value = AuthState.Unauthenticated
        }
    }

    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            authenticationState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        authenticationState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val userDocument = FirebaseFirestore.getInstance().collection("users").document(userId)
                        val userData = hashMapOf(
                            "friends" to listOf<String>(),
                            "habits" to listOf<String>()
                        )
                        userDocument.set(userData)
                            .addOnSuccessListener {
                                authenticationState.value = AuthState.Authenticated
                            }
                            .addOnFailureListener { exception ->
                                authenticationState.value = AuthState.Error(exception.message ?: "Firestore error")
                            }
                    }
                } else {
                    authenticationState.value = AuthState.Error(task.exception?.message?: "Unknown error")
                }
            }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            authenticationState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        authenticationState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    authenticationState.value = AuthState.Authenticated
                } else {
                    authenticationState.value = AuthState.Error(task.exception?.message?: "Unknown error")
                }
            }
    }


    fun signout() {
        auth.signOut()
        authenticationState.value = AuthState.Unauthenticated
    }


}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}