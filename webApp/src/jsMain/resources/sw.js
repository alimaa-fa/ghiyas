const CACHE_NAME = 'ghiyas-v1.0';

// نصب اولیه و فورس کردن به عنوان ورکر اصلی
self.addEventListener('install', (e) => {
    self.skipWaiting();
    e.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(['/', '/index.html', '/manifest.json']);
        })
    );
});

// پاکسازی نسخه‌های قدیمی هنگام آپدیت شما
self.addEventListener('activate', (e) => {
    e.waitUntil(
        caches.keys().then(keys => Promise.all(
            keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key))
        )).then(() => self.clients.claim())
    );
});

// رهگیری درخواست‌ها (استراتژی کش داینامیک)
self.addEventListener('fetch', (e) => {
    e.respondWith(
        caches.match(e.request).then(response => {
            // اگر در کش بود، همون رو بده (سرعت رعد و برق)
            if (response) return response;

            // اگر نبود، از اینترنت بگیر و به کش اضافه کن تا دفعه بعد آفلاین کار کنه
            return fetch(e.request).then(networkResponse => {
                if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
                    return networkResponse;
                }
                const responseToCache = networkResponse.clone();
                caches.open(CACHE_NAME).then(cache => {
                    // فقط فایل‌های دامنه خودمان را کش کن
                    if (e.request.url.startsWith(self.location.origin)) {
                        cache.put(e.request, responseToCache);
                    }
                });
                return networkResponse;
            }).catch(() => {
                // اینجا میشه صفحه خطای آفلاین رو هندل کرد
                console.log("آفلاین هستید و فایل در کش یافت نشد.");
            });
        })
    );
});
