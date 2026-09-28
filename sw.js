const CACHE_NAME = 'ghiyas-core-v56';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './manifest.json',
  './styles.css?v=56',
  './webApp.js',
  './icon-192.png',
  './icon-512.png',
  './fonts/DimaWeb.ttf'
];

self.addEventListener('install', (event) => {
  // خط طلایی: هیچ‌وقت در نسخه قدیمی منتظر تایید نمان، فوراً آپدیت را آماده کن
  self.skipWaiting();
  
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      // دور زدن کش مرورگر در زمان نصب برای دریافت تازه‌ترین فایل‌ها
      const requests = ASSETS_TO_CACHE.map(url => new Request(url, { cache: 'no-cache' }));
      return cache.addAll(requests);
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

// استراتژی ترکیبی (هیبریدی) برای سرعت و اطمینان
self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  const url = new URL(event.request.url);
  if (url.origin !== location.origin) return;

  // ۱. برای فایل اصلی (HTML): اولویت با شبکه (Network-First) تا آپدیت‌ها را فوری بگیرد
  if (event.request.mode === 'navigate' || url.pathname === '/' || url.pathname.endsWith('index.html')) {
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
  } else {
    // ۲. برای دارایی‌ها (CSS, JS): اولویت با کش (Cache-First) برای لود در کسری از ثانیه
    event.respondWith(
      caches.match(event.request).then((cachedResponse) => {
        if (cachedResponse) {
          return cachedResponse; 
        }
        return fetch(event.request).then((networkResponse) => {
          return caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, networkResponse.clone());
            return networkResponse;
          });
        });
      })
    );
  }
});
