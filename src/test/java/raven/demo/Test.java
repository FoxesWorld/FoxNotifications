package raven.demo;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.foxesworld.notification.Notification;
import org.foxesworld.notification.ui.ToastNotificationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class Test extends JFrame {

    private  Notification notification;
    public Test() {
        notification = new Notification();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 768);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new FlowLayout(FlowLayout.LEADING));
        JButton button = new JButton("Show");
        notification.setJFrame(this);
        CustomNotification customNotification = new CustomNotification();
        customNotification.setJFrame(this);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notification.show(getRandomType(), Notification.Location.TOP_RIGHT, getRandomText());
            }
        });
        JButton cmdMode = new JButton("Mode Light");
        cmdMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmdMode.getText().equals("Mode Light")) {
                    changeMode(true);
                    cmdMode.setText("Mode Dark");
                } else {
                    changeMode(false);
                    cmdMode.setText("Mode Light");
                }
            }
        });
        getContentPane().add(button);
        getContentPane().add(cmdMode);

        JButton buttonClear = new JButton("Clear");
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notification.clearAll();
            }
        });
        getContentPane().add(buttonClear);

        ToastNotificationPanel panel = new ToastNotificationPanel();
        panel.set(Notification.Type.INFO, "Hello my name is raven\nThis new Toast Panel Notification");
        this.notification.show(Notification.Type.INFO, new Rectangle(20,150, 300,45), 8000, "And my name is Aiden!");
        getContentPane().add(panel);
    }


    private Notification.Location getRandomLocation() {
        Random ran = new Random();
        int a = ran.nextInt(6);
        if (a == 0) {
            return Notification.Location.TOP_LEFT;
        } else if (a == 1) {
            return Notification.Location.TOP_CENTER;
        } else if (a == 2) {
            return Notification.Location.TOP_RIGHT;
        } else if (a == 3) {
            return Notification.Location.BOTTOM_LEFT;
        } else if (a == 4) {
            return Notification.Location.BOTTOM_CENTER;
        } else {
            return Notification.Location.BOTTOM_RIGHT;
        }
    }

    private String getRandomText() {
        Random ran = new Random();
        int a = ran.nextInt(5);
        if (a == 0) {
            return "Toast Notifications notify the user of a system occurrence";
        } else if (a == 1) {
            return "The notifications should have a consistent location in each application.\nWe recommend the top-right of the application";
        } else if (a == 2) {
            return "Toast Notifications notify the user of a system occurrence." +
                    "\nThe notifications should have a consistent location in each application." +
                    "\nWe recommend the top-right";
        } else if (a == 3) {
            return "Success";
        } else {
            return "Hello";
        }
    }

    private Notification.Type getRandomType() {
        Random ran = new Random();
        int a = ran.nextInt(4);
        if (a == 0) {
            return Notification.Type.SUCCESS;
        } else if (a == 1) {
            return Notification.Type.INFO;
        } else if (a == 2) {
            return Notification.Type.WARNING;
        } else {
            return Notification.Type.ERROR;
        }
    }


    private void changeMode(boolean dark) {
        if (FlatLaf.isLafDark() != dark) {
            if (dark) {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacDarkLaf.setup();
                    FlatLaf.updateUI();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                });
            } else {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacLightLaf.setup();
                    FlatLaf.updateUI();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                    ;
                });
            }
        }
    }

    public static void main(String[] args) {
        FlatLaf.registerCustomDefaultsSource("raven.toast");
        FlatMacLightLaf.setup();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Test().setVisible(true);
            }
        });
    }
}
