package gps.tracker.custom_overlays;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

import soturi.model.Position;

// Works like a human overlay, but is less annoying to deal with
public class CustomOverlay extends Marker {

    private static final Map<Drawable, Drawable> rescaleCache = new HashMap<>();

    public CustomOverlay(@NonNull MapView mapView, @NonNull Position position, @NonNull Drawable drawable) {
        super(mapView);

        Drawable icon = rescaleCache.computeIfAbsent(drawable, (ign) -> resizeDrawable(drawable, 20.0));

        this.setIcon(icon);
        this.setPosition(new GeoPoint(position.latitude(), position.longitude()));
        this.setAnchor(0.5f, 0.5f);
    }

    // From https://stackoverflow.com/questions/50077917/android-graphics-drawable-adaptiveicondrawable-cannot-be-cast-to-android-graphic
    // by Shashank Holla; CC BY-SA 4.0
    @NonNull
    private static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    @NonNull
    private Drawable resizeDrawable(Drawable drawable, double scale) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        int dstWidth = (int) (bitmap.getWidth() * scale);
        int dstHeight = (int) (bitmap.getHeight() * scale);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
        return new BitmapDrawable(resized);
    }

}
