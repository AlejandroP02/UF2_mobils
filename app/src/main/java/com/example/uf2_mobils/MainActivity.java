package com.example.uf2_mobils;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.uf2_mobils.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Actividad principal que controla la navegación
 * y la configuración de la interfaz de usuario.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Barra de navegación.
     */
    private AppBarConfiguration mAppBarConfiguration;
    /**
     * Binding para poder llamar a elementos del layout.
     */
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        /*binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configuración de la barra de navegación
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.homeFragment
                )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Configuración del encabezado de la barra de navegación
        View header = navigationView.getHeaderView(0);
        final ImageView photo = header.findViewById(R.id.photoImageView);
        final TextView name = header.findViewById(R.id.displayNameTextView);
        final TextView email = header.findViewById(R.id.emailTextView);

        // Listener para actualizar la información del usuario en el encabezado
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                if(user != null){
                    if (user.getPhotoUrl() == null){
                        DocumentReference userFromFirebase = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
                        userFromFirebase.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User usuario = documentSnapshot.toObject(User.class);

                                name.setText(usuario.getName());
                                email.setText(usuario.getEmail());
                                Glide.with(MainActivity.this).load(usuario.getMediaUri()).circleCrop().into(photo);
                            }
                        });
                    } else{
                        Glide.with(MainActivity.this)
                                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString())
                                .circleCrop()
                                .into(photo);
                    }
                    if (user.getDisplayName() == null){
                        name.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    }else {
                        name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    }
                    email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                }
            }
        });

        // Configuración de Firestore para no habilitar la persistencia de datos
        FirebaseFirestore.getInstance().setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú; esto agrega elementos a la barra de acciones si está presente.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}