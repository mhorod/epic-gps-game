package gps.tracker;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import androidx.fragment.app.Fragment;

import gps.tracker.databinding.FragmentQuestRewardsBinding;
import gps.tracker.views.ObjectWithDescription;
import soturi.model.Item;
import soturi.model.ItemId;
import soturi.model.Reward;

public class QuestRewards extends Fragment {

    FragmentQuestRewardsBinding binding;
    MainActivity mainActivity;
    Reward reward;

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
        reward = mainActivity.currentReward;

        ObjectWithDescription xp = new ObjectWithDescription(mainActivity, String.valueOf(reward.xp()), R.drawable.xp_mark, 15);
        xp.setGravity(Gravity.CENTER);

        binding.rewardInfo.addView(xp);

        Space space = new Space(mainActivity);
        space.setMinimumHeight(80);

        binding.rewardInfo.addView(space);

        for (ItemId item : reward.items()) {
            Item itemObject = mainActivity.getGameRegistry().getItemById(item);
            ItemChoice.FullItemInfoSegment itemInfo = new ItemChoice.FullItemInfoSegment(mainActivity, itemObject);

            Space itemSpace = new Space(mainActivity);
            itemSpace.setMinimumHeight(80);

            binding.rewardInfo.addView(itemSpace);
            binding.rewardInfo.addView(itemInfo);
        }
    }


}