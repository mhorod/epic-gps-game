package gps.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import gps.tracker.databinding.FragmentQuestRewardsBinding;

public class QuestRewards extends Fragment {

    FragmentQuestRewardsBinding binding;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentQuestRewardsBinding.inflate(getLayoutInflater());
        mainActivity = (MainActivity) getActivity();
    }


}