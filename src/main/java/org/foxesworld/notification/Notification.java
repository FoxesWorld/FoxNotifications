package org.foxesworld.notification;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.UIScale;
import org.foxesworld.notification.ui.ToastNotificationPanel;
import org.foxesworld.notification.util.NotificationHolder;
import org.foxesworld.notification.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Notification {

    private JFrame frame;
    private final Map<Location, List<NotificationAnimation>> lists = new HashMap<>();
    private final NotificationHolder notificationHolder = new NotificationHolder();
    private ComponentListener windowEvent;

    private void installEvent(JFrame frame) {
        if (windowEvent == null && frame != null) {
            windowEvent = new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    move(frame.getBounds());
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    move(frame.getBounds());
                }
            };
        }
        if (this.frame != null) {
            this.frame.removeComponentListener(windowEvent);
        }
        if (frame != null) {
            frame.addComponentListener(windowEvent);
        }
        this.frame = frame;
    }

    private int getCurrentShowCount(Location location) {
        List<NotificationAnimation> list = lists.get(location);
        return list == null ? 0 : list.size();
    }

    private synchronized void move(Rectangle rectangle) {
        for (Map.Entry<Location, List<NotificationAnimation>> set : lists.entrySet()) {
            for (NotificationAnimation an : set.getValue()) {
                if (an != null) {
                    an.move(rectangle);
                }
            }
        }
    }

    public void setJFrame(JFrame frame) {
        installEvent(frame);
    }

    public void show(Type type, String message) {
        show(type, Location.TOP_CENTER, message);
    }

    public void show(Type type, long duration, String message) {
        show(type, Location.TOP_CENTER, duration, message);
    }

    public void show(Type type, Location location, String message) {
        long duration = FlatUIUtils.getUIInt("Toast.duration", 2500);
        show(type, location, duration, message);
    }

    public void show(Type type, Location location, long duration, String message) {
        initStart(new NotificationAnimation(type, location, duration, message), duration);
    }

    public void show(Type type, Rectangle location, long duration, String message) {
        initStart(new NotificationAnimation(type, location, duration, message), duration);
    }

    public void show(JComponent component) {
        show(Location.TOP_CENTER, component);
    }

    public void show(Location location, JComponent component) {
        long duration = FlatUIUtils.getUIInt("Toast.duration", 2500);
        show(location, duration, component);
    }

    public void show(Location location, long duration, JComponent component) {
        initStart(new NotificationAnimation(location, duration, component), duration);
    }

    private synchronized boolean initStart(NotificationAnimation notificationAnimation, long duration) {
        int limit = FlatUIUtils.getUIInt("Toast.limit", -1);
        if (limit == -1 || getCurrentShowCount(notificationAnimation.getLocation()) < limit) {
            notificationAnimation.start();
            return true;
        } else {
            notificationHolder.hold(notificationAnimation);
            return false;
        }
    }

    private synchronized void notificationClose(NotificationAnimation notificationAnimation) {
        NotificationAnimation hold = notificationHolder.getHold(notificationAnimation.getLocation());
        if (hold != null) {
            if (initStart(hold, hold.getDuration())) {
                notificationHolder.removeHold(hold);
            }
        }
    }

    public void clearAll() {
        notificationHolder.clearHold();
        for (Map.Entry<Location, List<NotificationAnimation>> set : lists.entrySet()) {
            for (NotificationAnimation an : set.getValue()) {
                if (an != null) {
                    an.close();
                }
            }
        }
    }

    public void clear(Location location) {
        notificationHolder.clearHold(location);
        List<NotificationAnimation> list = lists.get(location);
        if (list != null) {
            for (NotificationAnimation an : list) {
                if (an != null) {
                    an.close();
                }
            }
        }
    }

    public void clearHold() {
        notificationHolder.clearHold();
    }

    public void clearHold(Location location) {
        notificationHolder.clearHold(location);
    }

    protected ToastNotificationPanel createNotification(Type type, String message) {
        ToastNotificationPanel toastNotificationPanel = new ToastNotificationPanel();
        toastNotificationPanel.set(type, message);
        return toastNotificationPanel;
    }

    private synchronized void updateList(Location key, NotificationAnimation values, boolean add) {
        if (add) {
            if (lists.containsKey(key)) {
                lists.get(key).add(values);
            } else {
                List<NotificationAnimation> list = new ArrayList<>();
                list.add(values);
                lists.put(key, list);
            }
        } else {
            if (lists.containsKey(key)) {
                lists.get(key).remove(values);
                if (lists.get(key).isEmpty()) {
                    lists.remove(key);
                }
            }
        }
    }

    public enum Type {
        SUCCESS, INFO, WARNING, ERROR
    }

    public enum Location {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    public class NotificationAnimation {

        private final JWindow window;
        private Animator animator;
        private boolean show = true;
        private float animate;
        private int x;
        private int y;
        private Location location;
        private Rectangle rectangle;
        private final long duration;
        private Insets frameInsets;
        private int horizontalSpace;
        private int animationMove;
        private boolean top;
        private boolean close = false;

        public NotificationAnimation(Type type, Location location, long duration, String message) {
            installDefault();
            this.location = location;
            this.duration = duration;
            window = new JWindow(frame);
            ToastNotificationPanel toastNotificationPanel = createNotification(type, message);
            toastNotificationPanel.putClientProperty(ToastClientProperties.TOAST_CLOSE_CALLBACK, (Consumer) o -> close());
            window.setContentPane(toastNotificationPanel);
            window.setFocusableWindowState(false);
            window.pack();
            toastNotificationPanel.setDialog(window);
        }

        public NotificationAnimation(Type type, Rectangle rectangle, long duration, String message) {
            installDefault();
            this.rectangle = rectangle;
            this.duration = duration;
            window = new JWindow(frame);
            ToastNotificationPanel toastNotificationPanel = createNotification(type, message);
            toastNotificationPanel.putClientProperty(ToastClientProperties.TOAST_CLOSE_CALLBACK, (Consumer) o -> close());
            window.setBackground(new Color(0, 0, 0, 0));
            window.setContentPane(toastNotificationPanel);
            window.setFocusableWindowState(false);
            window.pack();
            toastNotificationPanel.setDialog(window);

            Rectangle frameBounds = frame.getBounds();
            x = frameBounds.x + rectangle.x;
            y = frameBounds.y + rectangle.y;
            toastNotificationPanel.getWindow().setSize(rectangle.width, rectangle.height);
            window.setLocation(x, y);
        }

        public NotificationAnimation(Location location, long duration, JComponent component) {
            installDefault();
            this.location = location;
            this.duration = duration;
            window = new JWindow(frame);
            window.setBackground(new Color(0, 0, 0, 0));
            window.setContentPane(component);
            window.setFocusableWindowState(false);
            window.setSize(component.getPreferredSize());
        }

        private void installDefault() {
            frameInsets = UIUtils.getInsets("Toast.frameInsets", new Insets(10, 10, 10, 10));
            horizontalSpace = FlatUIUtils.getUIInt("Toast.horizontalGap", 10);
            animationMove = FlatUIUtils.getUIInt("Toast.animationMove", 10);
        }

        public void start() {
            int animation = FlatUIUtils.getUIInt("Toast.animation", 200);
            int resolution = FlatUIUtils.getUIInt("Toast.animationResolution", 5);
            animator = new Animator(animation, new Animator.TimingTarget() {
                @Override
                public void begin() {
                    if (show) {
                        updateList(location, NotificationAnimation.this, true);
                        installLocation();
                    }
                }

                @Override
                public void timingEvent(float f) {
                    animate = show ? f : 1f - f;
                    updateLocation(true);
                }

                @Override
                public void end() {
                    if (show && !close) {
                        SwingUtilities.invokeLater(() -> new Thread(() -> {
                            sleep(duration);
                            if (!close) {
                                show = false;
                                animator.start();
                            }
                        }).start());
                    } else {
                        updateList(location, NotificationAnimation.this, false);
                        window.dispose();
                        notificationClose(NotificationAnimation.this);
                    }
                }
            });
            animator.setResolution(resolution);
            animator.start();
        }

        private void installLocation() {
            if (rectangle != null) {
                // Координаты уже установлены в конструкторе
            } else {
                Insets insets = UIScale.scale(frameInsets);
                Rectangle rec = frame.getBounds();
                setupLocation(rec, insets);
            }
            window.setOpacity(0f);
            window.setVisible(true);
        }

        private void move(Rectangle rec) {
            Insets insets = UIScale.scale(frameInsets);
            setupLocation(rec, insets);
        }

        private void setupLocation(Rectangle rec, Insets insets) {
            if (rectangle != null) {
                // Координаты уже установлены в конструкторе
                return;
            }

            switch (location) {
                case TOP_LEFT -> {
                    x = rec.x + insets.left;
                    y = rec.y + insets.top;
                    top = true;
                }
                case TOP_CENTER -> {
                    x = rec.x + (rec.width - window.getWidth()) / 2;
                    y = rec.y + insets.top;
                    top = true;
                }
                case TOP_RIGHT -> {
                    x = rec.x + rec.width - (window.getWidth() + insets.right);
                    y = rec.y + insets.top;
                    top = true;
                }
                case BOTTOM_LEFT -> {
                    x = rec.x + insets.left;
                    y = rec.y + rec.height - (window.getHeight() + insets.bottom);
                    top = false;
                }
                case BOTTOM_CENTER -> {
                    x = rec.x + (rec.width - window.getWidth()) / 2;
                    y = rec.y + rec.height - (window.getHeight() + insets.bottom);
                    top = false;
                }
                case BOTTOM_RIGHT -> {
                    x = rec.x + rec.width - (window.getWidth() + insets.right);
                    y = rec.y + rec.height - (window.getHeight() + insets.bottom);
                    top = false;
                }
            }
            int am = UIScale.scale(top ? animationMove : -animationMove);
            int ly = (int) (getLocation(NotificationAnimation.this) + y + animate * am);
            window.setLocation(x, ly);
        }

        private void updateLocation(boolean loop) {
            int am = UIScale.scale(top ? animationMove : -animationMove);
            int ly = (int) (getLocation(NotificationAnimation.this) + y + animate * am);
            window.setLocation(x, ly);
            window.setOpacity(animate);
            if (loop) {
                update(this);
            }
        }

        private int getLocation(NotificationAnimation notification) {
            int height = 0;
            List<NotificationAnimation> list = lists.get(location);
            for (NotificationAnimation n : list) {
                if (notification == n) {
                    return height;
                }
                double v = n.animate * (n.window.getHeight() + UIScale.scale(horizontalSpace));
                height += top ? v : -v;
            }
            return height;
        }

        private void update(NotificationAnimation except) {
            List<NotificationAnimation> list = lists.get(location);
            for (NotificationAnimation n : list) {
                if (n != except) {
                    n.updateLocation(false);
                }
            }
        }

        public void close() {
            if (show) {
                if (animator.isRunning()) {
                    animator.stop();
                }
                close = true;
                show = false;
                animator.start();
            }
        }

        private void sleep(long l) {
            try {
                Thread.sleep(l);
            } catch (InterruptedException ignored) {
            }
        }

        public Location getLocation() {
            return location;
        }

        public long getDuration() {
            return duration;
        }
    }

}
