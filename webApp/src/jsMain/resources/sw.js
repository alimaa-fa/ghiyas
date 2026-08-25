const CACHE_NAME = 'ghiyas-core-v15';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './manifest.json',
  './styles.css?v=15',
  './webApp.js',
  './icon-192.png',
  './icon-512.png',
  './fonts/DimaWeb.ttf'
];

self.addEventListener('install', (event) => {
  // ❌ دستور skipWaiting حذف شد تا سرویس‌ورکر در صف انتظار بماند و دکمه آپدیت ظاهر شود
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
  const requestUrl = new URL(event.request.url);
  if (requestUrl.origin !== location.origin) return;

  // استراتژی Network-First برای HTML تا مرورگر همیشه متوجه آپدیت‌ها بشود
  if (event.request.mode === 'navigate') {
    event.respondWith(
      fetch(event.request).then((networkResponse) => {
        return caches.open(CACHE_NAME).then((cache) => {
          cache.put(event.request, networkResponse.clone());
          return networkResponse;
        });
      }).catch(() => {
        return caches.match('./index.html');
      })
    );
    return;
  }

  // استراتژی Cache-First برای بقیه فایل‌ها جهت لود سریع آفلاین
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      return cachedResponse || fetch(event.request).then((networkResponse) => {
        return caches.open(CACHE_NAME).then((cache) => {
          cache.put(event.request, networkResponse.clone());
          return networkResponse;
        });
      });
    })
  );
});

// این پیام با زدن دکمه «بروزرسانی» از سمت کاتلین ارسال می‌شود
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
