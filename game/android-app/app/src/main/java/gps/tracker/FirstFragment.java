package gps.tracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

import gps.tracker.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private Bitmap bitmap = null;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        System.out.println("onCreateView");
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        binding.textView.setText("Hello World");
        binding.textView.setOnTouchListener((x,d) -> {
            binding.textView.setText("TEEEEEST");
            return true;
        });

        binding.imageView.setOnClickListener(v -> {
            //binding.imageView.
            if (bitmap != null) {
                System.out.println("Bitmap not null");
                binding.imageView.setImageBitmap(bitmap);
            }
            new Thread(this::getImage).start();
        });

    }

    private synchronized void setBitmap(Bitmap bmp) {

        bitmap = bmp;

        System.out.println("Bitmap set");
    }

    public void getImage() {
        System.out.println("getImage");
        // Make HTTP request to get image
        // https://picsum.photos/200/300

        try {
            URL url = new URL("https://picsum.photos/200/300");
            InputStream in = url.openStream();

            System.out.println("in: " + in);
            Bitmap myBitmap = BitmapFactory.decodeStream(in);

            System.out.println(myBitmap.getHeight());

            System.out.println("myBitmap: " + myBitmap);

            setBitmap(myBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}