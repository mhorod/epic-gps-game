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

        binding.button.setOnClickListener(
                (e) -> {
                    String username = binding.editTextText.getText().toString();
                    String password = binding.editTextTextPassword.getText().toString();
                    mainActivity.runOnUiThread(
                            () -> {
                                mainActivity.login(username, password);
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
}