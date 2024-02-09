package com.example.uf2_mobils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragmento para el inicio de sesión.
 */
public class SignInFragment extends Fragment {

    /**
     * NavController para poder navegar entre layouts.
     */
    NavController navController;

    /**
     * Campos de entrada para el correo
     * electrónico y la contraseña.
     */
    private EditText emailEditText, passwordEditText;

    /**
     * Botón para iniciar sesión con correo
     * electrónico.
     */
    private Button emailSignInButton;

    /**
     * Diseño del formulario de inicio de sesión.
     */
    private LinearLayout signInForm;

    /**
     * Barra de progreso para mostrar el progreso
     * de inicio de sesión.
     */
    private ProgressBar signInProgressBar;

    /**
     * Instancia de FirebaseAuth para
     * la autenticación del usuario.
     */
    private FirebaseAuth mAuth;

    /**
     * Botón para iniciar sesión con Google.
     */
    private SignInButton googleSignInButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el diseño del fragmento
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // Configurar el evento onClick para ir al fragmento de registro
        view.findViewById(R.id.gotoCreateAccountTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.registerFragment);
            }
        });

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        emailSignInButton = view.findViewById(R.id.emailSignInButton);
        signInForm = view.findViewById(R.id.signInForm);
        signInProgressBar = view.findViewById(R.id.signInProgressBar);
        mAuth = FirebaseAuth.getInstance();

        signInProgressBar.setVisibility(View.GONE);

        // Configurar el evento onClick para iniciar sesión con correo electrónico
        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accederConEmail();
            }
        });

        googleSignInButton = view.findViewById(R.id.googleSignInButton);

        // Configurar el evento onClick para iniciar sesión con Google
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accederConGoogle();
            }
        });
    }

    /**
     * Método para iniciar sesión con correo electrónico y contraseña.
     */
    private void accederConEmail() {
        signInForm.setVisibility(View.GONE);
        signInProgressBar.setVisibility(View.VISIBLE);

        // Iniciar sesión con correo electrónico y contraseña en Firebase Authentication
        mAuth.signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            actualizarUI(mAuth.getCurrentUser());
                        } else {
                            Snackbar.make(requireView(), "Error: " + task.getException(), Snackbar.LENGTH_LONG).show();
                        }
                        signInForm.setVisibility(View.VISIBLE);
                        signInProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Método para actualizar la interfaz
     * de usuario después del inicio de sesión exitoso.
     */
    private void actualizarUI(FirebaseUser currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }

    /**
     * Método para iniciar sesión con Google.
     */
    private void accederConGoogle() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build());

        startActivityForResult(googleSignInClient.getSignInIntent(), 12345);
    }

    /**
     * Método para manejar el resultado
     * de la actividad de inicio de sesión con Google.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12345) {
            try {
                firebaseAuthWithGoogle(GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class));
            } catch (ApiException e) {
                Log.e("ABCD", "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    /**
     * Método para autenticar con Firebase
     * usando la credencial de Google.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if(acct == null) return;

        signInProgressBar.setVisibility(View.VISIBLE);
        signInForm.setVisibility(View.GONE);

        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("ABCD", "signInWithCredential:success");
                            actualizarUI(mAuth.getCurrentUser());
                            User user = new User(mAuth.getCurrentUser().getEmail(),passwordEditText.getText().toString(),mAuth.getCurrentUser().getUid(),mAuth.getCurrentUser().getPhotoUrl().toString());
                            FirebaseFirestore.getInstance().collection("users").document(user.getUID()).set(user);
                        } else {
                            Log.e("ABCD", "signInWithCredential:failure", task.getException());
                            signInProgressBar.setVisibility(View.GONE);
                            signInForm.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}