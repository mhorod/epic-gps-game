package soturi.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SusNoteApiController {
    private final List<String> content = new ArrayList<>();

    @PostMapping("/post-log")
    public synchronized void post(@RequestBody String text) {
        content.add(0, text);
        if (content.size() > 200)
            content.removeLast();
    }

    @GetMapping("/get-log")
    public synchronized List<String> get() {
        return content;
    }

    @PostMapping("/clear-log")
    public synchronized void clear() {
        content.clear();
    }
}
