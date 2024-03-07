package gps.tracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;

import gps.tracker.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private MainActivity mainActivity;
    private Bitmap bitmap = null;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        System.out.println("onCreateView");
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        mainActivity = (MainActivity) getActivity();

        return binding.getRoot();

    }

    private void runOnUiThread(Runnable r) {
        mainActivity.runOnUiThread(r);
    }

    private void locationListener(Location loc) {
        new Thread(() -> {
            Bitmap bmp = getImage(loc);
            System.out.println("bmp: " + bmp);
            runOnUiThread(() -> {
                binding.imageView.setImageBitmap(bmp);
            });
        }).start();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        mainActivity.registerLocationListener(this::locationListener);

    }

    public Bitmap getImage(Location loc) {
        try {
            URL url = new URL(
                    OpenStreetMapTranslator.get_tile_url(loc.getLatitude(), loc.getLongitude(), 18)
            );

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Soturi/0.1");

            connection.connect();

            InputStream in = connection.getInputStream();

            return BitmapFactory.decodeStream(in);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}