const CACHE_NAME = 'ghiyas-core-v14';
// فایل‌های اصلی (HTML و CSS) از اینجا حذف شدند تا همیشه اول از شبکه چک شوند
const ASSETS_TO_CACHE = [
  './manifest.json',
  './webApp.js',
  './icon-192.png',
  './icon-512.png',
  './fonts/DimaWeb.ttf'
];

self.addEventListener('install', (event) => {
  self.skipWaiting(); // جایگزینی فوری سرویس‌ورکر جدید
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE);
    })
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  
  const url = new URL(event.request.url);
  if (url.origin !== location.origin) return;

  // ۱. استراتژی Network-First برای HTML و CSS (آپدیت در لحظه)
  if (event.request.mode === 'navigate' || url.pathname.endsWith('.html') || url.pathname.endsWith('.css')) {
    event.respondWith(
      fetch(event.request).then((response) => {
        return caches.open(CACHE_NAME).then((cache) => {
          cache.put(event.request, response.clone());
          return response;
        });
      }).catch(() => caches.match(event.request)) // فال‌بک به نسخه آفلاین در صورت قطعی اینترنت
    );
    return;
  }

  // ۲. استراتژی Cache-First برای جاوااسکریپت، عکس‌ها و فونت‌ها (سرعت لود بالا)
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      if (cachedResponse) {
        // آپدیت نامحسوس در پس‌زمینه
        fetch(event.request).then((res) => {
          caches.open(CACHE_NAME).then((c) => c.put(event.request, res));
        }).catch(() => {});
        return cachedResponse;
      }
      return fetch(event.request).then((networkResponse) => {
        const clone = networkResponse.clone();
        caches.open(CACHE_NAME).then((c) => c.put(event.request, clone));
        return networkResponse;
      });
    })
  );
});
