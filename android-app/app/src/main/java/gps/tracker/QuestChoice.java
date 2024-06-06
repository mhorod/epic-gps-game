package gps.tracker;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import gps.tracker.databinding.FragmentQuestChoiceBinding;
import gps.tracker.views.ObjectWithDescription;
import soturi.model.QuestStatus;

public class QuestChoice extends Fragment {

    FragmentQuestChoiceBinding binding;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }

    private void createQuestInfo() {
        mainActivity.runOnUiThread(() -> {
            binding.deadlineInfo.setText("Deadline: " + mainActivity.getCurrentQuests().deadline());
        });

        List<QuestStatus> quests = mainActivity.getCurrentQuests().quests();

        for (QuestStatus quest : quests) {
            GraphicalQuest graphicalQuest = new GraphicalQuest(quest);
            Space space = new Space(mainActivity);
            space.setMinimumHeight(80);

            mainActivity.runOnUiThread(() -> {
                binding.questInfoLayout.addView(space);
                binding.questInfoLayout.addView(graphicalQuest);
            });
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentQuestChoiceBinding.inflate(getLayoutInflater());
        mainActivity = (MainActivity) getActivity();

        createQuestInfo();
    }

    private class GraphicalQuest extends LinearLayout {
        public GraphicalQuest(@NonNull QuestStatus quest) {
            super(mainActivity);

            this.setOrientation(LinearLayout.VERTICAL);

            TextView questGoal = new TextView(mainActivity);
            questGoal.setText(quest.quest());
            questGoal.setGravity(Gravity.CENTER_HORIZONTAL);

            LinearProgressIndicator progressBar = new LinearProgressIndicator(mainActivity);
            progressBar.setMax((int) quest.goal());
            progressBar.setProgress((int) quest.progress());
            progressBar.setIndeterminate(false);
            progressBar.setIndicatorColor(0xFF0000);
            progressBar.setForegroundGravity(Gravity.CENTER_HORIZONTAL);


            Space horizontalSpace1 = new Space(mainActivity);
            horizontalSpace1.setMinimumHeight(10);

            this.addView(questGoal);
            this.addView(horizontalSpace1);
            this.addView(progressBar);


            Space horizontalSpace2 = new Space(mainActivity);
            horizontalSpace2.setMinimumHeight(10);

            MaterialButton rewards = new MaterialButton(mainActivity);
            rewards.setText("Check rewards");
            rewards.setGravity(Gravity.CENTER_HORIZONTAL);
            rewards.setOnClickListener(
                    v -> {
                        mainActivity.runOnUiThread(
                                () -> {
                                    mainActivity.currentReward = quest.reward();
                                    NavHostFragment.findNavController(QuestChoice.this).navigate(R.id.action_questChoice_to_questRewards);
                                }
                        );
                    }
            );

            String finishedText = quest.isFinished() ? "Finished" : "Not finished";
            int markerId = quest.isFinished() ? R.drawable.checkmark : R.drawable.failmark;
            ObjectWithDescription finished = new ObjectWithDescription(mainActivity, finishedText, markerId, 20);

            LinearLayout rewardAndFinished = new LinearLayout(mainActivity);
            rewardAndFinished.setOrientation(LinearLayout.HORIZONTAL);
            rewardAndFinished.setGravity(Gravity.CENTER_HORIZONTAL);

            Space hspace = new Space(mainActivity);
            hspace.setMinimumWidth(10);

            rewardAndFinished.addView(finished);
            rewardAndFinished.addView(hspace);
            rewardAndFinished.addView(rewards);

            this.addView(horizontalSpace2);
            this.addView(rewardAndFinished);

        }
    }

}