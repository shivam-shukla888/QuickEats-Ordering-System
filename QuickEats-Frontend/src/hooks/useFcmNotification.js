import { useEffect, useState } from 'react';
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import { firebaseConfig, VAPID_KEY } from '../config/firebase';
import axiosInstance from '../api/axiosInstance';
import { useAuth } from '../context/AuthContext';

export const useFcmNotification = () => {
  const { isAuthenticated } = useAuth();
  const [fcmToken, setFcmToken] = useState(null);
  const [notificationBanner, setNotificationBanner] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) return;

    let unsubscribe = () => {};

    const setupFcm = async () => {
      try {
        if (!('Notification' in window)) {
          console.warn('Browser does not support desktop notifications.');
          return;
        }

        const permission = await Notification.requestPermission();
        if (permission !== 'granted') {
          console.info('Notification permission was denied or dismissed by user.');
          return;
        }

        const app = initializeApp(firebaseConfig);
        const messaging = getMessaging(app);

        // Fetch FCM Web Push Token
        const token = await getToken(messaging, { vapidKey: VAPID_KEY });
        if (token) {
          setFcmToken(token);
          // Register token with Spring Boot backend
          await axiosInstance.post('/api/notifications/register-token', { deviceToken: token });
          console.info('FCM Token registered with backend successfully.');
        }

        // Handle foreground notifications
        unsubscribe = onMessage(messaging, (payload) => {
          console.log('Foreground FCM Message received:', payload);
          setNotificationBanner({
            title: payload.notification?.title || 'QuickEats Order Update',
            body: payload.notification?.body || 'You have an update on your food order!'
          });

          // Auto dismiss banner after 6 seconds
          setTimeout(() => {
            setNotificationBanner(null);
          }, 6000);
        });
      } catch (err) {
        console.warn('FCM initialization or token registration failed gracefully:', err);
      }
    };

    setupFcm();

    return () => {
      unsubscribe();
    };
  }, [isAuthenticated]);

  return { fcmToken, notificationBanner, clearBanner: () => setNotificationBanner(null) };
};
