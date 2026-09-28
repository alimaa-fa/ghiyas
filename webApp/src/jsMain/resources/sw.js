const CACHE_NAME = 'ghiyas-core-v36';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './manifest.json',
  './styles.css?v=36',
  './webApp.js',
  './icon-192.png',
  './icon-512.png',
  './fonts/DimaWeb.ttf'
];

self.addEventListener('install', (event) => {
  // دستور self.skipWaiting() حذف شد تا منتظر فرمان کاربر بمانیم
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      // این خط طلایی است: به مرورگر می‌گوییم به هیچ وجه از کش داخلی (HTTP Cache) ایتا یا مرورگر
      // استفاده نکن و حتماً نسخه تازه را از سرور دانلود کن.
      const requests = ASSETS_TO_CACHE.map(url => new Request(url, { cache: 'no-cache' }));
      return cache.addAll(requests);
    })
  );
});

// گوش دادن به پیامِ تایید کاربر از سمت رابط کاربری وب‌اپ برای جایگزینی ورکر جدید
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
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

  event.respondWith(
    fetch(event.request).then((networkResponse) => {
      return caches.open(CACHE_NAME).then((cache) => {
        cache.put(event.request, networkResponse.clone());
        return networkResponse;
      });
    }).catch(() => {
      return caches.match(event.request).then((cachedResponse) => {
        return cachedResponse || caches.match('./index.html');
      });
    })
  );
});
