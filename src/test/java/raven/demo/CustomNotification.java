package raven.demo;

import com.formdev.flatlaf.FlatClientProperties;
import org.foxesworld.notification.Notification;
import org.foxesworld.notification.ToastClientProperties;
import org.foxesworld.notification.ui.ToastNotificationPanel;

import javax.swing.*;

public class CustomNotification extends Notification {
    @Override
    protected ToastNotificationPanel createNotification(Type type, String message) {
        ToastNotificationPanel toastNotificationPanel = super.createNotification(type, message);
        JLabel label = new JLabel(toastNotificationPanel.getKey(), toastNotificationPanel.getDefaultIcon(), JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setForeground(toastNotificationPanel.getDefaultColor());
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:$Notifications.font;" +
                "iconTextGap:0");
        toastNotificationPanel.putClientProperty(ToastClientProperties.TOAST_ICON, label);
        return toastNotificationPanel;
    }
}
