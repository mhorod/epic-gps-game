package gps.tracker.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

public class ObjectWithDescription extends LinearLayout {


    public ObjectWithDescription(Context context, String description, Bitmap bitmap, int spaceHeight) {
        super(context);

        this.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);
        this.addView(imageView);

        Space space = new Space(context);
        space.setMinimumHeight(spaceHeight);
        this.addView(space);

        TextView textView = new TextView(context);
        textView.setText(description);
        textView.setGravity(android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.CENTER_VERTICAL);
        this.addView(textView);

        this.setGravity(Gravity.CENTER_HORIZONTAL);
    }
    
    public ObjectWithDescription(Context context, String description, int imageResourceId, int spaceHeight) {
        super(context);

        this.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(imageResourceId);
        this.addView(imageView);

        Space space = new Space(context);
        space.setMinimumHeight(spaceHeight);
        this.addView(space);

        TextView textView = new TextView(context);
        textView.setText(description);
        textView.setGravity(android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.CENTER_VERTICAL);
        this.addView(textView);

        this.setGravity(Gravity.CENTER_HORIZONTAL);
    }
}
