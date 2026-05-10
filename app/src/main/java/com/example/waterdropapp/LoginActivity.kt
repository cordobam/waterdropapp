package com.example.waterdropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.waterdropapp.databinding.ActivityLoginBinding
import com.example.waterdropapp.ui.theme.WaterdropappTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        configurarGoogle()
        configurarBotones()
    }

    private fun configurarGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1051630695432-oaki27hfglhf6r0fe61vlvqbgk6n2tan.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun configurarBotones() {

        // EMAIL Y CONTRASEÑA
        binding.btnIngresar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    loginExitoso()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
        }

        // GOOGLE
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // REGISTRARSE
        //binding.tvRegistrate.setOnClickListener {
        //    startActivity(Intent(this, RegisterActivity::class.java))
        //}

        // EXPLORAR SIN CUENTA
        //binding.tvExplorarSinCuenta.setOnClickListener {
        //    startActivity(Intent(this, MarketplaceActivity::class.java))
        //    finish()
        //}

        // OLVIDÉ CONTRASEÑA
        binding.tvOlvidePassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresá tu email primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Te enviamos un email para restablecer tu contraseña", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No encontramos ese email", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // RESULTADO DEL LOGIN CON GOOGLE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthConGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("LOGIN_GOOGLE", "ApiException code: ${e.statusCode} - ${e.message}")
                Toast.makeText(this, "Código error: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthConGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                loginExitoso()
            }
            .addOnFailureListener { e ->
                // Cambiá el Toast por esto temporalmente
                Log.e("LOGIN_GOOGLE", "Error: ${e.message}")
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loginExitoso() {
        // Guardás el estado en SharedPreferences
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", true)
            .apply()

        // Vas al Marketplace y cerrás el login
        startActivity(Intent(this, MarketplaceActivity::class.java))
        finish()
    }
}