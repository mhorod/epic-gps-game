package gps.tracker.custom_overlays;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.annotation.NonNull;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import soturi.model.Position;

// Works like a human overlay, but is less annoying to deal with
public class CustomOverlay extends MyLocationNewOverlay {
    public CustomOverlay(MapView mapView, Position position, Drawable drawable) {
        super(mapView);
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        bitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true);
        setPersonIcon(bitmap);
        setMyLocationProvider(getTrivialLocationProvider(position));
        setPersonAnchor(0.5f, 0.5f);
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

    private static IMyLocationProvider getTrivialLocationProvider(Position position) {
        return new IMyLocationProvider() {

            @Override
            public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
                return true;
            }

            @Override
            public void stopLocationProvider() {

            }

            @Override
            public Location getLastKnownLocation() {
                Location l = new Location("");
                l.setLatitude(position.latitude());
                l.setLongitude(position.longitude());

                return l;
            }

            @Override
            public void destroy() {

            }
        };
    }

}
