'use strict';
const MANIFEST = 'flutter-app-manifest';
const TEMP = 'flutter-temp-cache';
const CACHE_NAME = 'flutter-app-cache';
const RESOURCES = {
  "version.json": "aabe4fc771c105a12e7e67f10fc638c3",
"index.html": "55c20824c82e0502b306e779821d9e78",
"/": "55c20824c82e0502b306e779821d9e78",
"main.dart.js": "46b27c317c28048174e3874710af3733",
"flutter.js": "f85e6fb278b0fd20c349186fb46ae36d",
"favicon.png": "5dcef449791fa27946b3d35ad8803796",
"icons/Icon-192.png": "ac9a721a12bbc803b44f645561ecb1e1",
"icons/Icon-maskable-192.png": "c457ef57daa1d16f64b27b786ec2ea3c",
"icons/Icon-maskable-512.png": "301a7604d45b3e739efc881eb04896ea",
"icons/Icon-512.png": "96e752610906ba2a93c65f8abe1645f1",
"manifest.json": "475a59d9adbe3ef43b447fc9bdc17014",
"assets/AssetManifest.json": "ee6ddbe359c53e2f48c035a1cd88706d",
"assets/NOTICES": "842e568fb6bd91c732fa776d52f86f1d",
"assets/FontManifest.json": "5a32d4310a6f5d9a6b651e75ba0d7372",
"assets/packages/cupertino_icons/assets/CupertinoIcons.ttf": "6d342eb68f170c97609e9da345464e5e",
"assets/packages/font_awesome_flutter/lib/fonts/fa-solid-900.ttf": "d8e9b6203ce2657c991f0b339ccb3a6d",
"assets/packages/font_awesome_flutter/lib/fonts/fa-regular-400.ttf": "48ce1bb8a42776caa951cb782d277730",
"assets/packages/font_awesome_flutter/lib/fonts/fa-brands-400.ttf": "99f29024aee8f4672a47cc3a81b9b84a",
"assets/shaders/ink_sparkle.frag": "29d2246bea15e3f64ee48c36dad0f204",
"assets/fonts/MaterialIcons-Regular.otf": "95db9098c58fd6db106f1116bae85a0b",
"assets/assets/images/svg/phone-call-end.svg": "a99d1f747dfee63ebc0a1bf8441e1d54",
"assets/assets/images/svg/no-video.svg": "739b40ff246a1906dd89c953ab05ca8a",
"assets/assets/images/svg/microphone.svg": "afa25edfd7b35f7699325a40571e978e",
"assets/assets/images/svg/reload.svg": "956633b0e885df9db3a39aae6cebafd1",
"assets/assets/images/svg/comment.svg": "a93a5733b7f42fafa37d05e4f7e8aeb0",
"assets/assets/images/svg/expand-tool.svg": "794494726f68f3e50408e3f15875755d",
"assets/assets/images/svg/upload.svg": "88136545a3adc8c8b86e77a9c5bee39c",
"assets/assets/images/svg/menu.svg": "874cd32ba7b10b335bfb0b9faa517940",
"assets/assets/images/svg/left.svg": "183a164326df0f8ddb1fbdcbf27b3e9e",
"assets/assets/images/match.gif": "3f87502bf3bae7dcd4539ee408381638",
"assets/assets/images/red_girl/idle_spriteSheet.png": "a97c44c557748f467dea4b3d31883b46",
"assets/assets/images/red_girl/gliding_spriteSheet.png": "7143b5a8e419d1ca41d0131e46875cb8",
"assets/assets/images/red_girl/jumping_spriteSheet.png": "eefc51c20f86fa6f23ae670da36abd14",
"assets/assets/images/red_girl/running_spriteSheet.png": "24cb1114b548be544df8864e2ba9cb64",
"assets/assets/images/red_girl/Kunai.png": "cfe19dbe81cbab4adebd17cf4ff9105d",
"assets/assets/images/red_girl/Tile_13.png": "d28e63fd837114f4e754f8e08b55265f",
"assets/assets/images/wait.gif": "950265cdf65d152bf3c7d8bb87b2a117",
"assets/assets/images/edit_profile.png": "bb30d7b4e0c5d269b914308980caa53d",
"assets/assets/images/matches.png": "15b7bf99234cec41bb142e5e8a6a18de",
"assets/assets/images/fortnight.png": "f2a9779c79f2c6153353905e799468e3",
"assets/assets/images/match.png": "a2611f1d658eb14281def7633dad4a41",
"assets/assets/images/Glide_007.png": "9a686aa13dde5106b2a452e08b4f11ee",
"assets/assets/images/panda.gif": "88bb1df66f0be9cae3c398dddbb5721e",
"assets/assets/images/dancer2.png": "3028f462e19eaefd6cf8d37c0e467cee",
"assets/assets/images/dancer.png": "a0a16cfc0ebb2b501145e537ba7f610c",
"canvaskit/canvaskit.js": "2bc454a691c631b07a9307ac4ca47797",
"canvaskit/profiling/canvaskit.js": "38164e5a72bdad0faa4ce740c9b8e564",
"canvaskit/profiling/canvaskit.wasm": "95a45378b69e77af5ed2bc72b2209b94",
"canvaskit/canvaskit.wasm": "bf50631470eb967688cca13ee181af62"
};

// The application shell files that are downloaded before a service worker can
// start.
const CORE = [
  "main.dart.js",
"index.html",
"assets/AssetManifest.json",
"assets/FontManifest.json"];
// During install, the TEMP cache is populated with the application shell files.
self.addEventListener("install", (event) => {
  self.skipWaiting();
  return event.waitUntil(
    caches.open(TEMP).then((cache) => {
      return cache.addAll(
        CORE.map((value) => new Request(value, {'cache': 'reload'})));
    })
  );
});

// During activate, the cache is populated with the temp files downloaded in
// install. If this service worker is upgrading from one with a saved
// MANIFEST, then use this to retain unchanged resource files.
self.addEventListener("activate", function(event) {
  return event.waitUntil(async function() {
    try {
      var contentCache = await caches.open(CACHE_NAME);
      var tempCache = await caches.open(TEMP);
      var manifestCache = await caches.open(MANIFEST);
      var manifest = await manifestCache.match('manifest');
      // When there is no prior manifest, clear the entire cache.
      if (!manifest) {
        await caches.delete(CACHE_NAME);
        contentCache = await caches.open(CACHE_NAME);
        for (var request of await tempCache.keys()) {
          var response = await tempCache.match(request);
          await contentCache.put(request, response);
        }
        await caches.delete(TEMP);
        // Save the manifest to make future upgrades efficient.
        await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
        return;
      }
      var oldManifest = await manifest.json();
      var origin = self.location.origin;
      for (var request of await contentCache.keys()) {
        var key = request.url.substring(origin.length + 1);
        if (key == "") {
          key = "/";
        }
        // If a resource from the old manifest is not in the new cache, or if
        // the MD5 sum has changed, delete it. Otherwise the resource is left
        // in the cache and can be reused by the new service worker.
        if (!RESOURCES[key] || RESOURCES[key] != oldManifest[key]) {
          await contentCache.delete(request);
        }
      }
      // Populate the cache with the app shell TEMP files, potentially overwriting
      // cache files preserved above.
      for (var request of await tempCache.keys()) {
        var response = await tempCache.match(request);
        await contentCache.put(request, response);
      }
      await caches.delete(TEMP);
      // Save the manifest to make future upgrades efficient.
      await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
      return;
    } catch (err) {
      // On an unhandled exception the state of the cache cannot be guaranteed.
      console.error('Failed to upgrade service worker: ' + err);
      await caches.delete(CACHE_NAME);
      await caches.delete(TEMP);
      await caches.delete(MANIFEST);
    }
  }());
});

// The fetch handler redirects requests for RESOURCE files to the service
// worker cache.
self.addEventListener("fetch", (event) => {
  if (event.request.method !== 'GET') {
    return;
  }
  var origin = self.location.origin;
  var key = event.request.url.substring(origin.length + 1);
  // Redirect URLs to the index.html
  if (key.indexOf('?v=') != -1) {
    key = key.split('?v=')[0];
  }
  if (event.request.url == origin || event.request.url.startsWith(origin + '/#') || key == '') {
    key = '/';
  }
  // If the URL is not the RESOURCE list then return to signal that the
  // browser should take over.
  if (!RESOURCES[key]) {
    return;
  }
  // If the URL is the index.html, perform an online-first request.
  if (key == '/') {
    return onlineFirst(event);
  }
  event.respondWith(caches.open(CACHE_NAME)
    .then((cache) =>  {
      return cache.match(event.request).then((response) => {
        // Either respond with the cached resource, or perform a fetch and
        // lazily populate the cache only if the resource was successfully fetched.
        return response || fetch(event.request).then((response) => {
          if (response && Boolean(response.ok)) {
            cache.put(event.request, response.clone());
          }
          return response;
        });
      })
    })
  );
});

self.addEventListener('message', (event) => {
  // SkipWaiting can be used to immediately activate a waiting service worker.
  // This will also require a page refresh triggered by the main worker.
  if (event.data === 'skipWaiting') {
    self.skipWaiting();
    return;
  }
  if (event.data === 'downloadOffline') {
    downloadOffline();
    return;
  }
});

// Download offline will check the RESOURCES for all files not in the cache
// and populate them.
async function downloadOffline() {
  var resources = [];
  var contentCache = await caches.open(CACHE_NAME);
  var currentContent = {};
  for (var request of await contentCache.keys()) {
    var key = request.url.substring(origin.length + 1);
    if (key == "") {
      key = "/";
    }
    currentContent[key] = true;
  }
  for (var resourceKey of Object.keys(RESOURCES)) {
    if (!currentContent[resourceKey]) {
      resources.push(resourceKey);
    }
  }
  return contentCache.addAll(resources);
}

// Attempt to download the resource online before falling back to
// the offline cache.
function onlineFirst(event) {
  return event.respondWith(
    fetch(event.request).then((response) => {
      return caches.open(CACHE_NAME).then((cache) => {
        cache.put(event.request, response.clone());
        return response;
      });
    }).catch((error) => {
      return caches.open(CACHE_NAME).then((cache) => {
        return cache.match(event.request).then((response) => {
          if (response != null) {
            return response;
          }
          throw error;
        });
      });
    })
  );
}
