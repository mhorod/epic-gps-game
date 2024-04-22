package gps.tracker.simple_listeners;

import java.util.ArrayList;
import java.util.List;

public class Notifier {
    private List<Listener> listeners = new ArrayList<>();

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (Listener listener : listeners) {
            listener.listen();
        }
    }
}
