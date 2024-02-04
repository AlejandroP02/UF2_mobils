package com.example.uf2_mobils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    ImageView photoImageView;
    TextView displayNameTextView, emailTextView;

    public ProfileFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getEmail().split("@")[0].toString();
        if (user.getDisplayName() != null){
            displayNameTextView.setText(user.getDisplayName());
        }
        else {
            displayNameTextView.setText(name);
        }
        emailTextView.setText(user.getEmail());
        if (user.getPhotoUrl() == null){
            Glide.with(requireView()).load(R.drawable.user).circleCrop().into(photoImageView);
        } else {
            Glide.with(requireView()).load(user.getPhotoUrl()).circleCrop().into(photoImageView);
        }
        if(user != null){
            displayNameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());

            Glide.with(requireView()).load(user.getPhotoUrl()).circleCrop().into(photoImageView);
        }
    }
}