package gps.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import gps.tracker.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    FragmentLoginBinding binding;
    MainActivity mainActivity;

    public LoginFragment() {
        // Required empty public constructor
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


        binding.loginButton.setOnClickListener(
                (e) -> {
                    String lambdaUsername = binding.editTextText.getText().toString();
                    String lambdaPassword = binding.editTextTextPassword.getText().toString();


                    mainActivity.saveString("username", lambdaUsername);
                    mainActivity.saveString("password", lambdaPassword);


                    mainActivity.runOnUiThread(
                            () -> {
                                mainActivity.login(lambdaUsername, lambdaPassword, false);
                                NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_gameMap);
                            }
                    );
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
        mainActivity.hideLocationKey();
    }
}