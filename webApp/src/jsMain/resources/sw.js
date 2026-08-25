const CACHE_NAME = 'ghiyas-core-v12';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './manifest.json',
  './styles.css?v=12',
  './webApp.js',
  './icon-192.png',
  './icon-512.png',
  './fonts/DimaWeb.ttf'
];

// نصب اولیه و ذخیره کلیه فایل‌ها در کش آفلاین
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE);
    })
  );
});

// فعال‌سازی و پاک‌سازی کش‌های منسوخ قبلی
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

// استراتژی کش پایدار برای لود ۱۰۰٪ آفلاین
self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;

  const requestUrl = new URL(event.request.url);
  // نادیده گرفتن درخواست‌های خارج از دامنه اپلیکیشن
  if (requestUrl.origin !== location.origin) {
    return;
  }

  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      if (cachedResponse) {
        // به‌روزرسانی نامحسوس کش در پس‌زمینه در صورت وجود اینترنت
        fetch(event.request).then((networkResponse) => {
          if (networkResponse && networkResponse.status === 200) {
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, networkResponse));
          }
        }).catch(() => {});
        return cachedResponse;
      }

      return fetch(event.request).then((networkResponse) => {
        if (!networkResponse || networkResponse.status !== 200) {
          return networkResponse;
        }
        const responseToCache = networkResponse.clone();
        caches.open(CACHE_NAME).then((cache) => {
          cache.put(event.request, responseToCache);
        });
        return networkResponse;
      }).catch(() => {
        if (event.request.mode === 'navigate') {
          return caches.match('./index.html');
        }
      });
    })
  );
});

// دریافت دستور کاربر برای فعال‌سازی نسخه جدید
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
