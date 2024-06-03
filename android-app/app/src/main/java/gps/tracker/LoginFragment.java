package gps.tracker;

import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import gps.tracker.databinding.FragmentLoginBinding;
import soturi.common.VersionInfo;

public class LoginFragment extends Fragment {

    FragmentLoginBinding binding;
    MainActivity mainActivity;

    public LoginFragment() {
        // Required empty public constructor
    }

    public void initVersionInfo() {
        String text = "Compilation time: " + VersionInfo.compilationTime + "\nCommit id: " + VersionInfo.commitId;
        binding.textView.setText(text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentLoginBinding.inflate(getLayoutInflater());
        mainActivity = (MainActivity) getActivity();

        String username = mainActivity.getString("username");
        String password = mainActivity.getString("password");

        if (username != null && password != null) {
            binding.editTextText.setText(username);
            binding.editTextTextPassword.setText(password);
        }

        initVersionInfo();

        binding.loginButton.setOnClickListener(
                (e) -> {
                    String lambdaUsername = binding.editTextText.getText().toString();
                    String lambdaPassword = binding.editTextTextPassword.getText().toString();


                    mainActivity.saveString("username", lambdaUsername);
                    mainActivity.saveString("password", lambdaPassword);

                    ProgressDialog gpsProgress = new ProgressDialog(this.mainActivity);
                    gpsProgress.setTitle("Waiting for GPS signal...");
                    gpsProgress.setMessage("Please wait...");
                    gpsProgress.setCancelable(false);

                    ProgressDialog loginProgress = new ProgressDialog(this.mainActivity);
                    loginProgress.setTitle("Logging in");
                    loginProgress.setMessage("Spawning frogs...");
                    loginProgress.setCancelable(false);

                    mainActivity.registerLocationListener(
                            new LocationListener() {
                                @Override
                                public void onLocationChanged(@NonNull Location location) {
                                    mainActivity.removeLocationListener(this);
                                    mainActivity.runOnUiThread(() -> {
                                                gpsProgress.dismiss();
                                                loginProgress.show();
                                            }
                                    );
                                }
                            }
                    );

                    mainActivity.setOnLoggedIn(
                            () -> mainActivity.runOnUiThread(
                                    () -> {
                                        loginProgress.dismiss();
                                        NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_gameMap);
                                    }
                            )
                    );
                    
                    mainActivity.runOnUiThread(
                            gpsProgress::show
                    );

                    mainActivity.login(lambdaUsername, lambdaPassword, false);
                }
        );

        binding.devLoginButton.setOnClickListener(
                (e) -> {
                    String lambdaUsername = binding.editTextText.getText().toString();
                    String lambdaPassword = binding.editTextTextPassword.getText().toString();


                    mainActivity.saveString("username", lambdaUsername);
                    mainActivity.saveString("password", lambdaPassword);

                    mainActivity.runOnUiThread(
                            () -> {
                                mainActivity.login(lambdaUsername, lambdaPassword, true);
                                NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_gameMap);
                            }
                    );
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity.logout();
    }
}