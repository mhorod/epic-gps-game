package soturi.server.communication;

import lombok.experimental.UtilityClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;

@UtilityClass
public class BlockingQueuePoll {
    private static Method POLL;
    private static Object SECONDS;

    @SuppressWarnings("unchecked")
    private <T> T pollChecked(BlockingQueue<T> queue, long timeoutInSeconds) throws Exception {
        if (POLL == null) {
            Class<?> timeUnitClass = Class.forName("java.util.concurrent.TimeUnit");
            SECONDS = timeUnitClass.getField("SECONDS").get(null);
            POLL = BlockingQueue.class.getMethod("poll", long.class, timeUnitClass);
        }
        return (T) POLL.invoke(queue, timeoutInSeconds, SECONDS);
    }
    /**
     * Used reflection because I hate red squiggly lines in IntelliJ
     * <a href="https://youtrack.jetbrains.com/issue/IDEA-321080/Cannot-resolve-symbol-TimeUnit-in-JDK-21">bug</a>.
     * <p>
     * Equivalent to {@code queue.poll(timeoutInSeconds, TimeUnit.SECONDS)}.
     */
    public <T> T poll(BlockingQueue<T> queue, long timeoutInSeconds) throws InterruptedException {
        try {
            return pollChecked(queue, timeoutInSeconds);
        }
        catch (Exception e) {
            if (e instanceof InvocationTargetException ite)
                if (ite.getTargetException() instanceof InterruptedException ie)
                    throw ie;
            throw new RuntimeException(e);
        }
    }
}
