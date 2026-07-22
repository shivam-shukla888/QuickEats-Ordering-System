importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-messaging-compat.js');

firebase.initializeApp({
  projectId: "quickeats-2e295",
  messagingSenderId: "109444248170932492112",
  appId: "1:109444248170932492112:web:quickeats"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Received background message: ', payload);
  const notificationTitle = payload.notification.title || 'QuickEats Order Update';
  const notificationOptions = {
    body: payload.notification.body || 'You have a new update on your food order!',
    icon: '/favicon.ico'
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});
