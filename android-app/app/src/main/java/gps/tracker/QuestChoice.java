package gps.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import gps.tracker.databinding.FragmentQuestChoiceBinding;

public class QuestChoice extends Fragment {

    FragmentQuestChoiceBinding binding;
    MainActivity mainActivity;
    ItemManager itemManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return binding.getRoot();
    }


}