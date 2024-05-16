package gps.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import gps.tracker.databinding.FragmentItemChoiceBinding;
import soturi.model.Item;
import soturi.model.messages_to_server.EquipItem;

public class ItemChoice extends Fragment {

    FragmentItemChoiceBinding binding;
    MainActivity mainActivity;
    ItemManager itemManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentItemChoiceBinding.inflate(getLayoutInflater());
        mainActivity = (MainActivity) getActivity();
        itemManager = mainActivity.getItemManager();

        Item.ItemType type = itemManager.getItemTypeOfInterest();

        if (type == null) {
            // We have a problem. This should never happen.
            mainActivity.runOnUiThread(
                    // Go back
                    NavHostFragment.findNavController(ItemChoice.this)::popBackStack
            );
        }

        List<Item> items = itemManager.getItemsOfType(type);

        for (Item item : items) {
            int spaceWidth = 10;
            int maxImageWidth = 16;
            // Time for some perfectly normal UI code with no issues whatsoever

            LinearLayout layout = new LinearLayout(mainActivity);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(spaceWidth, 2 * spaceWidth, spaceWidth, 2 * spaceWidth);

            layout.setLayoutParams(layoutParams);

            TextView name = new TextView(mainActivity);
            name.setText(item.name());
            name.setGravity(View.TEXT_ALIGNMENT_CENTER);
            name.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(name);

            Space space1 = new Space(mainActivity);
            space1.setMinimumWidth(spaceWidth);
            layout.addView(space1);

            ImageView hpImage = new ImageView(mainActivity);
            hpImage.setImageResource(R.drawable.heart);
            hpImage.setForegroundGravity(View.TEXT_ALIGNMENT_CENTER);
            hpImage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            hpImage.setMaxWidth(maxImageWidth);
            layout.addView(hpImage);

            Space space2 = new Space(mainActivity);
            space2.setMinimumWidth(spaceWidth);
            layout.addView(space2);

            TextView hp = new TextView(mainActivity);
            hp.setText(String.valueOf(item.statistics().maxHp()));
            hp.setGravity(View.TEXT_ALIGNMENT_CENTER);
            hp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(hp);

            Space space3 = new Space(mainActivity);
            space3.setMinimumWidth(spaceWidth);
            layout.addView(space3);

            ImageView attackImage = new ImageView(mainActivity);
            attackImage.setImageResource(R.drawable.sword);
            attackImage.setForegroundGravity(View.TEXT_ALIGNMENT_CENTER);
            attackImage.setMaxWidth(maxImageWidth);
            layout.addView(attackImage);

            Space space4 = new Space(mainActivity);
            space4.setMinimumWidth(spaceWidth);
            layout.addView(space4);


            TextView attack = new TextView(mainActivity);
            attack.setText(String.valueOf(item.statistics().attack()));
            attack.setGravity(View.TEXT_ALIGNMENT_CENTER);
            attack.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(attack);

            Space space5 = new Space(mainActivity);
            space5.setMinimumWidth(spaceWidth);
            layout.addView(space5);

            ImageView defenseImage = new ImageView(mainActivity);
            defenseImage.setImageResource(R.drawable.shield);
            defenseImage.setForegroundGravity(View.TEXT_ALIGNMENT_CENTER);
            defenseImage.setMaxWidth(maxImageWidth);
            layout.addView(defenseImage);

            Space space6 = new Space(mainActivity);
            space6.setMinimumWidth(spaceWidth);
            layout.addView(space6);

            TextView defense = new TextView(mainActivity);
            defense.setText(String.valueOf(item.statistics().defense()));
            defense.setGravity(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(defense);

            Space space7 = new Space(mainActivity);
            space7.setMinimumWidth(spaceWidth);
            layout.addView(space7);

            MaterialButton equipButton = new MaterialButton(mainActivity);
            equipButton.setText("Equip");
            equipButton.setGravity(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(equipButton);

            equipButton.setOnClickListener(
                    v -> {
                        mainActivity.getWebSocketClient().sendMessage(new EquipItem(item.itemId()));

                        System.out.println("Layout has " + layout.getChildCount() + " children.");

                        for (int i = 0; i < layout.getChildCount(); i++) {
                            View child = layout.getChildAt(i);
                            if (child instanceof LinearLayout linlayout) {
                                System.out.println("LinearLayout found.");
                                for (int j = 0; j < linlayout.getChildCount(); j++) {
                                    View grandchild = linlayout.getChildAt(j);
                                    if (grandchild instanceof MaterialButton button) {
                                        System.out.println("Button found.");
                                        mainActivity.runOnUiThread(
                                                () -> {
                                                    button.setIcon(null);
                                                    button.invalidate();
                                                }
                                        );
                                    }
                                }
                            }
                        }

                        mainActivity.runOnUiThread(
                                () -> {
                                    equipButton.setIcon(getResources().getDrawable(R.drawable.checkmark));
                                }
                        );
                    }
            );

            mainActivity.runOnUiThread(
                    () -> binding.itemChoiceLayout.addView(layout)
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return binding.getRoot();
    }


}