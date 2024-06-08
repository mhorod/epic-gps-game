package gps.tracker.custom_overlays;

import android.graphics.Bitmap;
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

    @NonNull
    private Drawable resizeDrawable(Drawable drawable, double scale) {
        BitmapDrawable bitmap = (BitmapDrawable) drawable;
        int dstWidth = (int) (bitmap.getIntrinsicWidth() * scale);
        int dstHeight = (int) (bitmap.getIntrinsicHeight() * scale);
        Bitmap beforeResizing = bitmap.getBitmap();
        Bitmap resized = Bitmap.createScaledBitmap(beforeResizing, dstWidth, dstHeight, false);
        return new BitmapDrawable(resized);
    }

}
