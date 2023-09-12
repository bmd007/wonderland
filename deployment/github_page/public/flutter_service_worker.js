'use strict';
const MANIFEST = 'flutter-app-manifest';
const TEMP = 'flutter-temp-cache';
const CACHE_NAME = 'flutter-app-cache';

const RESOURCES = {"assets/assets/images/dancer2.png": "3028f462e19eaefd6cf8d37c0e467cee",
"assets/assets/images/match.png": "a2611f1d658eb14281def7633dad4a41",
"assets/assets/images/edit_profile.png": "bb30d7b4e0c5d269b914308980caa53d",
"assets/assets/images/matches.png": "15b7bf99234cec41bb142e5e8a6a18de",
"assets/assets/images/fortnight.png": "f2a9779c79f2c6153353905e799468e3",
"assets/assets/images/red_girl/jumping_spriteSheet.png": "eefc51c20f86fa6f23ae670da36abd14",
"assets/assets/images/red_girl/idle_spriteSheet.png": "a97c44c557748f467dea4b3d31883b46",
"assets/assets/images/red_girl/gliding_spriteSheet.png": "7143b5a8e419d1ca41d0131e46875cb8",
"assets/assets/images/red_girl/running_spriteSheet.png": "24cb1114b548be544df8864e2ba9cb64",
"assets/assets/images/red_girl/Kunai.png": "cfe19dbe81cbab4adebd17cf4ff9105d",
"assets/assets/images/wait.gif": "950265cdf65d152bf3c7d8bb87b2a117",
"assets/assets/images/background.jpeg": "1a2982e8eacca1be9921ec83fe1098f1",
"assets/assets/images/dancer.png": "a0a16cfc0ebb2b501145e537ba7f610c",
"assets/assets/images/Objects/Switch%2520(1).png": "def7f815e54b9da248a6f62bb71f86ad",
"assets/assets/images/Objects/Switch%2520(2).png": "45635e5103ad6c687c05586df87f210b",
"assets/assets/images/Objects/Barrel%2520(2).png": "ad4dba9c0247a5144a104eb500c98157",
"assets/assets/images/Objects/DoorOpen.png": "5b982e68835217a098e22c78fd9c9b53",
"assets/assets/images/Objects/Barrel%2520(1).png": "a581ea8b9d910348699d037b1d1bc76e",
"assets/assets/images/Objects/DoorLocked.png": "380c54528d344bb29cff25f215bdadc2",
"assets/assets/images/Objects/Box.png": "ef11f8106bb9a87986bab42afec8de0c",
"assets/assets/images/Objects/DoorUnlocked.png": "c116f17b74eba48570accb6702829952",
"assets/assets/images/Objects/Saw.png": "611d41a19ce784e4638e3b0747354698",
"assets/assets/images/Tiles/Tile_13.png": "d28e63fd837114f4e754f8e08b55265f",
"assets/assets/images/Tiles/Tile%2520(6).png": "5c1cfce8d127f36da814dee7f7a5715f",
"assets/assets/images/Tiles/BGTile%2520(7).png": "d98465021109dbbc31ea1bf9a05b9e18",
"assets/assets/images/Tiles/Tile%2520(2).png": "57be9f7c4661da524ffd0e0d417b788b",
"assets/assets/images/Tiles/Tile%2520(14).png": "c477b8d3c29ee88fde0ccd5f96dd2952",
"assets/assets/images/Tiles/Tile%2520(3).png": "70cb133e8dd586265161d6d59b393350",
"assets/assets/images/Tiles/Tile%2520(8).png": "4fa4d895989bcee02e46b6382e95af1c",
"assets/assets/images/Tiles/BGTile%2520(5).png": "f864da1be15baaf1767c9187f38685d0",
"assets/assets/images/Tiles/Spike.png": "43c856d02dee078470c497ca82448a95",
"assets/assets/images/Tiles/Tile%2520(7).png": "865e91fd5195cfdb551a7019c3aef7fa",
"assets/assets/images/Tiles/BGTile%2520(1).png": "9bda6cc3a7e0f550997b118c66d4daee",
"assets/assets/images/Tiles/Tile%2520(15).png": "cc7d3ec471da3c8effa050702759a5d4",
"assets/assets/images/Tiles/Tile%2520(12).png": "bf29d43fb699cde72df7288658bb1e52",
"assets/assets/images/Tiles/Acid%2520(1).png": "5ef1319498a26f030303cf16d5c01fce",
"assets/assets/images/Tiles/Tile%2520(5).png": "5ae8bf5c20d79761e4990b34c4c92abc",
"assets/assets/images/Tiles/Fence%2520(3).png": "82b163df8fc6b1207124a1337bd91b36",
"assets/assets/images/Tiles/BGTile%2520(6).png": "f5cc8d831837b1a0e0b7f99b1777f77b",
"assets/assets/images/Tiles/Tile%2520(10).png": "bd621ddcf6253411e20cfe4374df9957",
"assets/assets/images/Tiles/BGTile%2520(4).png": "6801e05f02b7f67e6b497f9d89cf8dd3",
"assets/assets/images/Tiles/Tile%2520(11).png": "3d2495bbbbce2d463e3e12c5c87bcd33",
"assets/assets/images/Tiles/Tile%2520(4).png": "4e7fc60cbf35aa2a2a91a40e9e8f2122",
"assets/assets/images/Tiles/Tile%2520(9).png": "6d9390a3a7e57fad0576bad7aecf3e28",
"assets/assets/images/Tiles/Tile%2520(1).png": "f9fa68519a3c0e1cd692c5912a690df6",
"assets/assets/images/Tiles/BGTile%2520(2).png": "11fca541a74b4c4619c0632d79466f43",
"assets/assets/images/Tiles/Fence%2520(1).png": "91a5dce58bf481f9003b3bef4ad4dd0d",
"assets/assets/images/Tiles/Fence%2520(2).png": "ed9457faf525ce04963538c965120f21",
"assets/assets/images/Tiles/Acid%2520(2).png": "97c0cbbdc9e49918b3d2b102cf9f3438",
"assets/assets/images/Tiles/BGTile%2520(3).png": "b0a0f8a476bb0af4c361a28fdacaffe0",
"assets/assets/images/Glide_007.png": "9a686aa13dde5106b2a452e08b4f11ee",
"assets/assets/images/match.gif": "3f87502bf3bae7dcd4539ee408381638",
"assets/assets/images/TeamGunner/EXTRAS/SpongeBullet.png": "8674b070bb9948327b77ccbb0af7feb1",
"assets/assets/images/TeamGunner/EXTRAS/Platform.png": "bb389762186474b06e9639059a9bb73d",
"assets/assets/images/TeamGunner/EXTRAS/MuzzleFlash.png": "96e3e9ad55642f8be979c5ec9754787c",
"assets/assets/images/TeamGunner/EXTRAS/BulletStream.png": "3e324b1ab15070a07a55eb0a73ba92d0",
"assets/assets/images/TeamGunner/EXTRAS/Platform_Thin.png": "299f1597df8a29a37800936693416661",
"assets/assets/images/TeamGunner/EXTRAS/Shadow.png": "9d6fe1083bf761ebd0d67cb29ed2e2e6",
"assets/assets/images/TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Crouch.png": "180ae34eca16723d9c0cd8adb4cee583",
"assets/assets/images/TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Jump.png": "fa45f2b1bb0f3a03e1eb75709ff71a53",
"assets/assets/images/TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Death.png": "58fa2ffc8144dae76bbac56edeb698c4",
"assets/assets/images/TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Idle.png": "4bd1fb30830bdd797cb36234535fb39d",
"assets/assets/images/TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Run.png": "ef9fd3b85221b84f2f716909652b40c8",
"assets/assets/images/panda.gif": "88bb1df66f0be9cae3c398dddbb5721e",
"assets/assets/images/svg/menu.svg": "874cd32ba7b10b335bfb0b9faa517940",
"assets/assets/images/svg/left.svg": "183a164326df0f8ddb1fbdcbf27b3e9e",
"assets/assets/images/svg/expand-tool.svg": "794494726f68f3e50408e3f15875755d",
"assets/assets/images/svg/phone-call-end.svg": "a99d1f747dfee63ebc0a1bf8441e1d54",
"assets/assets/images/svg/reload.svg": "956633b0e885df9db3a39aae6cebafd1",
"assets/assets/images/svg/upload.svg": "88136545a3adc8c8b86e77a9c5bee39c",
"assets/assets/images/svg/comment.svg": "a93a5733b7f42fafa37d05e4f7e8aeb0",
"assets/assets/images/svg/microphone.svg": "afa25edfd7b35f7699325a40571e978e",
"assets/assets/images/svg/no-video.svg": "739b40ff246a1906dd89c953ab05ca8a",
"assets/assets/images/green_girl/jumping_spriteSheet.png": "32756c31d897aac12d0f5cff605827ae",
"assets/assets/images/green_girl/idle_spriteSheet.png": "b5abc1ab03871132a46c7a93ea101348",
"assets/assets/images/green_girl/gliding_spriteSheet.png": "02afd19710e32c9ca67560111568e77d",
"assets/assets/images/green_girl/running_spriteSheet.png": "c9bcf34b3a3be0a1b07121452e17bee3",
"assets/assets/images/green_girl/Kunai.png": "677c5dbd92a71fa502853dede748dd6c",
"assets/fonts/MaterialIcons-Regular.otf": "051823d3de8e06e85b506757ccc5c7e6",
"assets/AssetManifest.json": "2849d41d8c87a0bab8390d4fb3d9fd65",
"assets/packages/font_awesome_flutter/lib/fonts/fa-regular-400.ttf": "5070443340d1d8cceb516d02c3d6dee7",
"assets/packages/font_awesome_flutter/lib/fonts/fa-brands-400.ttf": "d7791ef376c159f302b8ad90a748d2ab",
"assets/packages/font_awesome_flutter/lib/fonts/fa-solid-900.ttf": "658b490c9da97710b01bd0f8825fce94",
"assets/packages/cupertino_icons/assets/CupertinoIcons.ttf": "89ed8f4e49bcdfc0b5bfc9b24591e347",
"assets/shaders/ink_sparkle.frag": "f8b80e740d33eb157090be4e995febdf",
"assets/AssetManifest.bin": "c4da82bd1947e3b9f8fc0338f76b1c4a",
"assets/FontManifest.json": "5a32d4310a6f5d9a6b651e75ba0d7372",
"assets/NOTICES": "e6723bfec3f1175b3c3c7365fdc20ef5",
"version.json": "aabe4fc771c105a12e7e67f10fc638c3",
"manifest.json": "475a59d9adbe3ef43b447fc9bdc17014",
"index.html": "3b99164cd89114cd27b353b26e005c69",
"/": "3b99164cd89114cd27b353b26e005c69",
"favicon.png": "5dcef449791fa27946b3d35ad8803796",
"flutter.js": "6fef97aeca90b426343ba6c5c9dc5d4a",
"icons/Icon-192.png": "ac9a721a12bbc803b44f645561ecb1e1",
"icons/Icon-maskable-192.png": "c457ef57daa1d16f64b27b786ec2ea3c",
"icons/Icon-512.png": "96e752610906ba2a93c65f8abe1645f1",
"icons/Icon-maskable-512.png": "301a7604d45b3e739efc881eb04896ea",
"main.dart.js": "a0a887775734db7489cdc659b8159ea0",
"canvaskit/canvaskit.wasm": "d9f69e0f428f695dc3d66b3a83a4aa8e",
"canvaskit/skwasm.wasm": "d1fde2560be92c0b07ad9cf9acb10d05",
"canvaskit/canvaskit.js": "5caccb235fad20e9b72ea6da5a0094e6",
"canvaskit/skwasm.worker.js": "51253d3321b11ddb8d73fa8aa87d3b15",
"canvaskit/chromium/canvaskit.wasm": "393ec8fb05d94036734f8104fa550a67",
"canvaskit/chromium/canvaskit.js": "ffb2bb6484d5689d91f393b60664d530",
"canvaskit/skwasm.js": "95f16c6690f955a45b2317496983dbe9"};
// The application shell files that are downloaded before a service worker can
// start.
const CORE = ["main.dart.js",
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
        // Claim client to enable caching on first launch
        self.clients.claim();
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
      // Claim client to enable caching on first launch
      self.clients.claim();
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
