const app = document.getElementById("app");

function showFatalError(message) {
  if (!app) return;
  app.innerHTML = `
    <div class="container py-5">
      <div class="alert alert-danger border-0 shadow-sm">
        <strong>No se pudo cargar la interfaz.</strong>
        <div class="mt-2 small">${safe(message, "Error desconocido")}</div>
      </div>
    </div>
  `;
}

const state = {
  sessionUser: null,
  users: [],
  products: [],
  games: [],
  achievements: [],
  unlockedAchievements: [],
  orders: [],
  platforms: [],
  steamProfile: null,
  steamLibrary: [],
  steamSyncResult: null,
  steamError: "",
  steamSearch: "",
  adminEdit: null,
  cart: [],
  loading: false,
  error: ""
};

const safe = (v, fallback = "-") => (v === null || v === undefined || v === "" ? fallback : v);
const points = (v) => `${Number(v || 0).toLocaleString("es-ES")} monedas`;

const GUEST_ROUTES = new Set(["/", "/catalogo", "/acceso", "/registro", "/olvide-password"]);
const USER_ROUTES = new Set([
  "/usuario/dashboard",
  "/usuario/perfil",
  "/usuario/carrito",
  "/usuario/canjes",
  "/usuario/logros",
  "/usuario/configuracion"
]);
const ADMIN_ROUTES = new Set([
  "/admin/dashboard",
  "/admin/usuarios",
  "/admin/productos",
  "/admin/videojuegos",
  "/admin/logros",
  "/admin/canjes",
  "/admin/plataformas",
  "/admin/configuracion"
]);

function navigate(path) {
  if (window.location.pathname !== path) {
    window.history.pushState({}, "", path);
  }
  route();
}

function isAdmin() {
  return state.sessionUser && String(state.sessionUser.rol).toUpperCase() === "ADMIN";
}

function getInitial(name) {
  if (!name) return "U";
  return String(name).trim().charAt(0).toUpperCase();
}

function getAdminEditing(resource, id) {
  if (!state.adminEdit || state.adminEdit.resource !== resource) return null;
  return String(state.adminEdit.id) === String(id) ? state.adminEdit.item : null;
}

function startAdminEdit(resource, id, item) {
  state.adminEdit = { resource, id, item };
  route();
}

function cancelAdminEdit() {
  state.adminEdit = null;
  route();
}

function isEditingResource(resource) {
  return state.adminEdit && state.adminEdit.resource === resource;
}

async function apiGet(url) {
  const res = await fetch(url, { credentials: "same-origin" });
  if (!res.ok) {
    throw new Error(`Error ${res.status} en ${url}`);
  }
  return res.json();
}

async function apiPost(url, body) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "same-origin",
    body: JSON.stringify(body)
  });

  if (!res.ok) {
    let msg = `Error ${res.status}`;
    try {
      const payload = await res.json();
      msg = payload.error || msg;
    } catch {
      const text = await res.text();
      if (text) msg = text;
    }
    throw new Error(msg);
  }

  return res.json();
}

async function apiPut(url, body) {
  const res = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    credentials: "same-origin",
    body: JSON.stringify(body)
  });

  if (!res.ok) {
    let msg = `Error ${res.status}`;
    try {
      const payload = await res.json();
      msg = payload.error || msg;
    } catch {
      const text = await res.text();
      if (text) msg = text;
    }
    throw new Error(msg);
  }

  return res.json();
}

async function apiDelete(url) {
  const res = await fetch(url, {
    method: "DELETE",
    credentials: "same-origin"
  });

  if (!res.ok) {
    let msg = `Error ${res.status}`;
    try {
      const payload = await res.json();
      msg = payload.error || msg;
    } catch {
      const text = await res.text();
      if (text) msg = text;
    }
    throw new Error(msg);
  }

  if (res.status === 204 || res.status === 205) return null;
  try {
    return await res.json();
  } catch {
    return null;
  }
}

async function fetchSessionUser() {
  try {
    state.sessionUser = await apiGet("/api/auth/me");
  } catch {
    state.sessionUser = null;
  }
}

async function loadSteamData() {
  state.steamProfile = null;
  state.steamLibrary = [];
  state.steamError = "";

  if (!state.sessionUser) return;

  try {
    state.steamProfile = await apiGet("/api/steam/me");
  } catch (e) {
    state.steamError = e.message || "No se pudo leer el perfil de Steam";
    state.steamProfile = null;
  }

  try {
    const library = await apiGet("/api/steam/library");
    state.steamLibrary = Array.isArray(library) ? library : [];
  } catch (e) {
    state.steamError = e.message || "No se pudo leer la biblioteca de Steam";
    state.steamLibrary = [];
  }
}

async function loadData() {
  state.loading = true;
  state.error = "";

  try {
    const [users, products, games, achievements, unlocked, orders, platforms] = await Promise.all([
      apiGet("/api/usuarios"),
      apiGet("/api/productos"),
      apiGet("/api/videojuegos"),
      apiGet("/api/logros"),
      apiGet("/api/logros-desbloqueados"),
      apiGet("/api/canjes"),
      apiGet("/api/plataformas")
    ]);

    state.users = Array.isArray(users) ? users : [];
    state.products = Array.isArray(products) ? products : [];
    state.games = Array.isArray(games) ? games : [];
    state.achievements = Array.isArray(achievements) ? achievements : [];
    state.unlockedAchievements = Array.isArray(unlocked) ? unlocked : [];
    state.orders = Array.isArray(orders) ? orders : [];
    state.platforms = Array.isArray(platforms) ? platforms : [];
  } catch (e) {
    state.error = e.message || "No se pudieron cargar los datos";
  } finally {
    state.loading = false;
  }
}

function topNav() {
  const authLinks = state.sessionUser
    ? `${!isAdmin() ? `<li class="nav-item"><a class="nav-link" href="/usuario/dashboard" data-link>Mi zona</a></li>` : ""}
       ${isAdmin() ? `<li class="nav-item"><a class="nav-link" href="/admin/dashboard" data-link>Admin</a></li>` : ""}
       <li class="nav-item"><a class="nav-link" href="#" data-action="logout">Salir</a></li>`
    : `<li class="nav-item"><a class="nav-link" href="/acceso" data-link>Acceso</a></li>
       <li class="nav-item"><a class="nav-link" href="/registro" data-link>Registro</a></li>`;

  return `
    <nav class="navbar navbar-expand-lg topbar px-3">
      <div class="container-fluid">
        <a class="navbar-brand fw-bold" href="/" data-link>TrophyShop</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
          <ul class="navbar-nav ms-auto gap-2">
            <li class="nav-item"><a class="nav-link" href="/" data-link>Inicio</a></li>
            <li class="nav-item"><a class="nav-link" href="/catalogo" data-link>Catalogo</a></li>
            ${authLinks}
          </ul>
        </div>
      </div>
    </nav>
  `;
}

function shell(title, body) {
  return `
    <div class="app-shell min-vh-100 d-flex flex-column">
      ${topNav()}
      <main class="container py-4 flex-grow-1">
        <header class="mb-4"><h1 class="h3 mb-1">${title}</h1></header>
        ${state.loading ? `<div class="alert alert-info border-0">Cargando...</div>` : ""}
        ${state.error ? `<div class="alert alert-danger border-0">${state.error}</div>` : ""}
        ${body}
      </main>
      <footer class="py-3 text-center text-secondary small">TrophyShop</footer>
    </div>
  `;
}

function productCard(p) {
  return `
    <article class="col-md-6 col-xl-4">
      <div class="card h-100 product-card border-0 shadow-sm">
        <div class="card-body d-flex flex-column">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="badge badge-soft">producto</span>
            <span class="small text-secondary">Stock: ${safe(p.stock, 0)}</span>
          </div>
          <h5 class="card-title">${safe(p.nombre)}</h5>
          <p class="card-text text-secondary">${safe(p.descripcion, "Sin descripcion")}</p>
          <div class="mt-auto d-flex justify-content-between align-items-end">
            <div class="fw-bold">${points(p.costoMonedas)}</div>
            <div class="d-flex gap-2">
              <button class="btn btn-outline-primary btn-sm" data-action="detail-product" data-id="${p.id}">Detalle</button>
              ${state.sessionUser ? `<button class="btn btn-primary btn-sm" data-action="add-cart" data-id="${p.id}">Canjear</button>` : ""}
            </div>
          </div>
        </div>
      </div>
    </article>
  `;
}

function gameCard(g) {
  return `
    <article class="col-md-6 col-xl-4">
      <div class="card h-100 product-card border-0 shadow-sm">
        <div class="card-body d-flex flex-column">
          <span class="badge badge-soft mb-2">videojuego</span>
          <h5 class="card-title">${safe(g.titulo)}</h5>
          <p class="card-text text-secondary">Plataforma: ${safe(g.plataforma?.nombre, "N/A")}</p>
          <p class="card-text text-secondary">Usuario: ${safe(g.usuario?.nombre, "N/A")}</p>
          <p class="card-text text-secondary">Steam AppID: ${safe(g.steamAppId, "No asignado")}</p>
          <div class="mt-auto d-flex justify-content-end">
            <button class="btn btn-outline-primary btn-sm" data-action="detail-game" data-id="${g.id}">Detalle</button>
          </div>
        </div>
      </div>
    </article>
  `;
}

function renderHome() {
  return shell(
    "TrophyShop",
    `
      <section class="hero p-4 p-md-5 rounded-4 mb-4">
        <div class="row align-items-center g-4">
          <div class="col-lg-7">
            <p class="text-uppercase text-secondary small mb-2">Bienvenido a TrophyShop</p>
            <h2 class="display-5 fw-bold">Merchandising para gamers, recompensas reales y tu progreso Steam en un solo lugar.</h2>
            <p class="lead text-secondary mt-3">Explora la tienda de merchandising, sincroniza tu cuenta Steam y convierte tus logros desbloqueados en monedas virtuales para canjes y ventajas dentro de TrophyShop.</p>
            <div class="d-flex gap-2 flex-wrap">
              <a href="/catalogo" class="btn btn-primary" data-link>Ver catalogo</a>
              ${state.sessionUser ? `<a href="/usuario/logros" class="btn btn-outline-primary" data-link>Ver mis logros</a>` : `<a href="/acceso" class="btn btn-outline-primary" data-link>Iniciar sesion</a>`}
            </div>
          </div>
          <div class="col-lg-5">
            <div class="stat-panel rounded-4 p-3 p-md-4">
              <p class="mb-2 fw-semibold">Como funciona</p>
              <div class="row g-2">
                <div class="col-12"><div class="kpi-card"><span>1. Entra con Steam</span><strong>Sincroniza tu perfil</strong></div></div>
                <div class="col-12"><div class="kpi-card"><span>2. Desbloquea logros</span><strong>Gana monedas</strong></div></div>
                <div class="col-12"><div class="kpi-card"><span>3. Canjea merchandising</span><strong>Consigue recompensas</strong></div></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="row g-3 mb-4">
        <div class="col-md-4">
          <div class="card border-0 shadow-sm h-100 p-3">
            <h3 class="h5">Tienda de merchandising</h3>
            <p class="text-secondary mb-0">Camisetas, tazas y productos pensados para la comunidad gamer.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card border-0 shadow-sm h-100 p-3">
            <h3 class="h5">Steam conectado</h3>
            <p class="text-secondary mb-0">Vincula tu cuenta y consulta juegos, logros y progreso desde tu perfil real de Steam.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card border-0 shadow-sm h-100 p-3">
            <h3 class="h5">Monedas y canjes</h3>
            <p class="text-secondary mb-0">Cada logro desbloqueado suma monedas para que conviertas tu actividad en recompensas.</p>
          </div>
        </div>
      </section>

      <section class="card border-0 shadow-sm p-4">
        <div class="row align-items-center g-3">
          <div class="col-lg-8">
            <h3 class="h4 mb-2">Diseñado para jugadores, no para paneles técnicos</h3>
            <p class="text-secondary mb-0">La portada te orienta rápido: qué es TrophyShop, qué puedes hacer y por dónde empezar. Si ya tienes Steam, solo entra y explora tus logros y recompensas.</p>
          </div>
          <div class="col-lg-4 text-lg-end">
            <a href="/acceso" class="btn btn-primary" data-link>Empezar ahora</a>
          </div>
        </div>
      </section>
    `
  );
}

function renderCatalogo() {
  const productsHtml = state.products.map(productCard).join("");
  return shell(
    "Catalogo de merchandising",
    `
      <div class="row g-3">${productsHtml || `<div class="col-12"><div class="alert alert-secondary border-0">No hay productos.</div></div>`}</div>
    `
  );
}

function renderAcceso() {
  if (state.sessionUser) {
    navigate(isAdmin() ? "/admin/dashboard" : "/usuario/dashboard");
    return "";
  }

  return shell(
    "Acceso",
    `
      <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
          <form class="card border-0 shadow-sm p-4" id="loginForm">
            <label class="form-label">Email</label>
            <input class="form-control mb-3" type="email" name="email" required>
            <label class="form-label">Contrasena</label>
            <input class="form-control mb-3" type="password" name="password" required>
            <button class="btn btn-primary w-100" type="submit">Entrar</button>
            <a class="btn btn-outline-primary w-100 mt-2" href="/api/steam/login">Entrar con Steam</a>
            <div class="mt-3 d-flex justify-content-between small">
              <a href="/registro" data-link>Crear cuenta</a>
              <a href="/olvide-password" data-link>Olvide mi contrasena</a>
            </div>
          </form>
        </div>
      </div>
    `
  );
}

function renderRegistro() {
  return shell(
    "Registro",
    `
      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <form class="card border-0 shadow-sm p-4" id="registerForm">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Nombre</label>
                <input class="form-control" name="nombre" required>
              </div>
              <div class="col-md-6">
                <label class="form-label">Email</label>
                <input class="form-control" type="email" name="email" required>
              </div>
              <div class="col-12">
                <label class="form-label">Contrasena</label>
                <input class="form-control" type="password" name="password" required>
              </div>
            </div>
            <button class="btn btn-primary mt-4" type="submit">Crear cuenta</button>
          </form>
        </div>
      </div>
    `
  );
}

function renderForgot() {
  return shell(
    "Recuperar contrasena",
    `<div class="alert alert-secondary border-0">Aun no hay endpoint de recuperacion. Usa /registro o /acceso.</div>`
  );
}

function userLayout(title, body) {
  return shell(
    title,
    `
      <div class="row g-4">
        <aside class="col-lg-3">
          <div class="card border-0 shadow-sm p-3">
            <div class="d-flex align-items-center gap-3 mb-3">
              <div class="avatar d-flex align-items-center justify-content-center fw-bold">${getInitial(state.sessionUser?.nombre)}</div>
              <div>
                <div class="fw-semibold">${safe(state.sessionUser?.nombre, "Sin usuario")}</div>
                <div class="small text-secondary">${points(state.sessionUser?.monedasAcumuladas)}</div>
              </div>
            </div>
            <nav class="nav flex-column gap-1">
              <a class="nav-link" href="/usuario/dashboard" data-link>Dashboard</a>
              <a class="nav-link" href="/usuario/perfil" data-link>Perfil</a>
              <a class="nav-link" href="/usuario/carrito" data-link>Carrito</a>
              <a class="nav-link" href="/usuario/canjes" data-link>Canjes</a>
              <a class="nav-link" href="/usuario/logros" data-link>Logros</a>
              <a class="nav-link" href="/usuario/configuracion" data-link>Configuracion</a>
            </nav>
          </div>
        </aside>
        <section class="col-lg-9">${body}</section>
      </div>
    `
  );
}

function renderUserDashboard() {
  return userLayout(
    "Dashboard de usuario",
    `<div class="row g-3 mb-3"><div class="col-md-4"><div class="kpi-card h-100"><span>Monedas</span><strong>${points(state.sessionUser?.monedasAcumuladas)}</strong></div></div><div class="col-md-4"><div class="kpi-card h-100"><span>Canjes</span><strong>${state.orders.length}</strong></div></div><div class="col-md-4"><div class="kpi-card h-100"><span>Logros</span><strong>${state.achievements.length}</strong></div></div></div>`
  );
}

function renderUserPerfil() {
  const steamBlock = state.steamProfile
    ? `<div class="mt-3 p-3 rounded-3" style="background: var(--color-bg-soft);">
         <p class="mb-1"><strong>Steam:</strong> Vinculado</p>
         <p class="mb-1"><strong>SteamID:</strong> ${safe(state.steamProfile.steamId)}</p>
         <p class="mb-0"><strong>Perfil Steam:</strong> <a href="${safe(state.steamProfile.profileUrl, "#")}" target="_blank" rel="noreferrer">${safe(state.steamProfile.personaName)}</a></p>
       </div>`
    : `<div class="mt-3"><a class="btn btn-outline-primary" href="/api/steam/login">Vincular cuenta Steam</a></div>`;

  return userLayout(
    "Perfil",
    `<div class="card border-0 shadow-sm p-4"><p><strong>Nombre:</strong> ${safe(state.sessionUser?.nombre)}</p><p><strong>Email:</strong> ${safe(state.sessionUser?.email)}</p><p><strong>Rol:</strong> ${safe(state.sessionUser?.rol)}</p>${steamBlock}</div>`
  );
}

function renderUserCarrito() {
  const items = state.cart
    .map((id) => state.products.find((p) => String(p.id) === String(id)))
    .filter(Boolean);

  if (!items.length) {
    return userLayout("Carrito", `<div class="alert alert-secondary border-0">No hay productos en el carrito.</div>`);
  }

  const total = items.reduce((sum, i) => sum + Number(i.costoMonedas || 0), 0);
  return userLayout(
    "Carrito",
    `<div class="card border-0 shadow-sm p-3"><table class="table"><thead><tr><th>Producto</th><th>Monedas</th><th></th></tr></thead><tbody>${items.map((i) => `<tr><td>${safe(i.nombre)}</td><td>${points(i.costoMonedas)}</td><td class="text-end"><button class="btn btn-sm btn-outline-danger" data-action="remove-cart" data-id="${i.id}">Quitar</button></td></tr>`).join("")}</tbody></table><div class="d-flex justify-content-between"><strong>Total: ${points(total)}</strong><button class="btn btn-primary" data-action="confirm-cart">Confirmar canje</button></div></div>`
  );
}

function renderUserCanjes() {
  return userLayout(
    "Canjes",
    `<div class="card border-0 shadow-sm p-3"><table class="table"><thead><tr><th>ID</th><th>Producto</th><th>Total</th><th>Fecha</th></tr></thead><tbody>${state.orders.map((o) => `<tr><td>${safe(o.id)}</td><td>${safe(o.producto?.nombre)}</td><td>${points(o.totalMonedas)}</td><td>${safe(o.fechaCanje)}</td></tr>`).join("") || `<tr><td colspan="4" class="text-secondary">No hay canjes.</td></tr>`}</tbody></table></div>`
  );
}

function renderUserLogros() {
  const normalizedSearch = String(state.steamSearch || "").trim().toLowerCase();
  const filteredLibrary = normalizedSearch
    ? state.steamLibrary.filter((g) => String(g.name || "").toLowerCase().includes(normalizedSearch))
    : state.steamLibrary;

  const totalUnlocked = filteredLibrary.reduce((sum, g) => sum + Number(g.unlockedCount || 0), 0);
  const totalProjectedPoints = filteredLibrary.reduce((sum, g) => sum + Number(g.totalPoints || 0), 0);

  const steamStatus = state.steamProfile
    ? `<div class="alert alert-info border-0 d-flex justify-content-between align-items-center">
         <div>
           <strong>Steam vinculado:</strong> ${safe(state.steamProfile.personaName)}
           <div class="small text-secondary">Juegos visibles: ${filteredLibrary.length} de ${state.steamLibrary.length} | Logros desbloqueados: ${totalUnlocked}</div>
         </div>
         <button class="btn btn-primary btn-sm" data-action="sync-steam">Sincronizar logros Steam</button>
       </div>`
    : `<div class="alert alert-secondary border-0">Conecta tu cuenta Steam para sincronizar logros y sumar monedas. <a href="/api/steam/login">Entrar con Steam</a></div>`;

  const syncResult = state.steamSyncResult
    ? `<div class="alert alert-success border-0">Sincronizacion completada: +${points(state.steamSyncResult.grantedPoints)} | Logros nuevos: ${safe(state.steamSyncResult.newAchievements, 0)} | Comunes: ${safe(state.steamSyncResult.commonAchievements, 0)} | Raros: ${safe(state.steamSyncResult.rareAchievements, 0)}</div>`
    : "";

  const steamError = state.steamError
    ? `<div class="alert alert-warning border-0">${safe(state.steamError)}. Si Steam no devuelve tus juegos, revisa en Steam: perfil publico + detalles de juegos publicos.</div>`
    : "";

  const searchBlock = `
    <div class="card border-0 shadow-sm p-3 mb-3">
      <label class="form-label mb-2" for="steamGameSearch">Buscar juego por nombre</label>
      <input id="steamGameSearch" class="form-control" type="search" placeholder="Ej: Counter, GTA, Elden..." value="${safe(state.steamSearch, "")}">
    </div>
  `;

  const libraryPreview = filteredLibrary.length
    ? `<div class="card border-0 shadow-sm p-3 mb-3"><h3 class="h6 mb-2">Resumen Steam</h3><p class="mb-2">Puntos potenciales por logros visibles: <strong>${points(totalProjectedPoints)}</strong></p><div class="small text-secondary">Puntos por logro: comun = 200, raro = 500</div></div>
       <div class="steam-library-grid">${filteredLibrary.map((g) => `<article class="steam-game-card"><img class="steam-game-image" src="https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${safe(g.appId, 0)}/header.jpg" alt="${safe(g.name, "Juego Steam")}" loading="lazy" onerror="this.src='https://placehold.co/460x215/F2F2F2/123859?text=Sin+portada'" /><div class="steam-game-body"><div class="d-flex justify-content-between align-items-start gap-2 mb-2"><h3 class="h6 mb-0">${safe(g.name, "App " + safe(g.appId))}</h3><span class="small text-secondary">AppID ${safe(g.appId)}</span></div><div class="small text-secondary mb-2">Tiempo jugado: ${safe(g.playtimeMinutes, 0)} min</div><div class="steam-points-row mb-2"><span class="badge text-bg-light">Logros: ${safe(g.unlockedCount, 0)}</span><span class="badge text-bg-light">${points(g.totalPoints)}</span></div><div class="steam-achievements-list">${(Array.isArray(g.achievements) ? g.achievements : []).map((a) => `<div class="steam-achievement-item"><div><strong>${safe(a.displayName, a.apiName)}</strong><div class="small text-secondary">${a.rarityType === "RARE" ? "Raro" : "Comun"} · ${safe(a.rarityPercent, 100)}%</div></div><span class="steam-achievement-points">${points(a.points)}</span></div>`).join("") || `<div class="small text-secondary">Este juego no muestra logros desbloqueados para este usuario.</div>`}</div></div></article>`).join("")}</div>`
    : state.steamLibrary.length
      ? `<div class="alert alert-secondary border-0">No hay juegos que coincidan con "${safe(state.steamSearch, "")}".</div>`
      : `<div class="alert alert-secondary border-0">Steam no devolvio juegos para esta cuenta. Revisa /api/steam/debug/current para ver el diagnostico y confirmar que Steam devuelve game_count > 0.</div>`;

  return userLayout(
    "Logros",
    `${steamStatus}${steamError}${syncResult}${searchBlock}${libraryPreview}`
  );
}

function renderUserConfig() {
  return userLayout("Configuracion", `<div class="alert alert-secondary border-0">Configuracion disponible.</div>`);
}

function optionTags(items, valueGetter, labelGetter, selectedValue) {
  return items
    .map((item) => {
      const value = valueGetter(item);
      const selected = String(value) === String(selectedValue) ? "selected" : "";
      return `<option value="${safe(value)}" ${selected}>${safe(labelGetter(item))}</option>`;
    })
    .join("");
}

function adminEditBadge(resource) {
  return isEditingResource(resource)
    ? `<span class="badge text-bg-warning ms-2">Editando</span>`
    : "";
}

function adminFormActions(resource) {
  return `
    <div class="d-flex gap-2 mt-3">
      <button class="btn btn-primary" type="submit">${isEditingResource(resource) ? "Guardar cambios" : "Crear"}</button>
      ${isEditingResource(resource) ? `<button class="btn btn-outline-secondary" type="button" data-action="admin-cancel">Cancelar</button>` : ""}
    </div>
  `;
}

function adminRowActions(resource, item) {
  return `
    <button class="btn btn-sm btn-outline-primary" data-action="admin-edit" data-resource="${resource}" data-id="${item.id}">Editar</button>
    <button class="btn btn-sm btn-outline-danger" data-action="admin-delete" data-resource="${resource}" data-id="${item.id}">Borrar</button>
  `;
}

function findAdminItem(resource, id) {
  const lookup = {
    usuarios: state.users,
    productos: state.products,
    plataformas: state.platforms,
    videojuegos: state.games,
    logros: state.achievements,
    canjes: state.orders
  }[resource] || [];
  return lookup.find((item) => String(item.id) === String(id)) || null;
}

function renderAdminDashboard() {
  return adminLayout(
    "Admin Dashboard",
    `
      <div class="row g-3 mb-4">
        <div class="col-md-3"><div class="kpi-card"><span>Usuarios</span><strong>${state.users.length}</strong></div></div>
        <div class="col-md-3"><div class="kpi-card"><span>Productos</span><strong>${state.products.length}</strong></div></div>
        <div class="col-md-3"><div class="kpi-card"><span>Logros</span><strong>${state.achievements.length}</strong></div></div>
        <div class="col-md-3"><div class="kpi-card"><span>Canjes</span><strong>${state.orders.length}</strong></div></div>
      </div>
      <div class="row g-3">
        <div class="col-md-4"><a class="card border-0 shadow-sm p-3 text-decoration-none text-dark h-100" href="/admin/usuarios" data-link><strong>Gestionar usuarios</strong><div class="text-secondary small">Crear, editar y borrar usuarios.</div></a></div>
        <div class="col-md-4"><a class="card border-0 shadow-sm p-3 text-decoration-none text-dark h-100" href="/admin/productos" data-link><strong>Gestionar merchandising</strong><div class="text-secondary small">Alta y edición de productos.</div></a></div>
        <div class="col-md-4"><a class="card border-0 shadow-sm p-3 text-decoration-none text-dark h-100" href="/admin/logros" data-link><strong>Gestionar logros</strong><div class="text-secondary small">Asigna logros a videojuegos locales o Steam.</div></a></div>
        <div class="col-md-4"><a class="card border-0 shadow-sm p-3 text-decoration-none text-dark h-100" href="/admin/videojuegos" data-link><strong>Gestionar videojuegos</strong><div class="text-secondary small">Mapea juegos Steam con AppID.</div></a></div>
        <div class="col-md-4"><a class="card border-0 shadow-sm p-3 text-decoration-none text-dark h-100" href="/admin/plataformas" data-link><strong>Gestionar plataformas</strong><div class="text-secondary small">Crea o elimina plataformas.</div></a></div>
        <div class="col-md-4"><a class="card border-0 shadow-sm p-3 text-decoration-none text-dark h-100" href="/admin/canjes" data-link><strong>Gestionar canjes</strong><div class="text-secondary small">Revisa y borra canjes.</div></a></div>
      </div>
    `
  );
}

function renderAdminUsuarios() {
  const editing = isEditingResource("usuarios") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Usuarios${adminEditBadge("usuarios")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminUsuariosForm" data-resource="usuarios" data-id="${safe(editing?.id || "")}">
            <h3 class="h6">${editing ? "Editar usuario" : "Nuevo usuario"}</h3>
            <label class="form-label mt-2">Nombre</label>
            <input class="form-control" name="nombre" value="${safe(editing?.nombre, "")}" required>
            <label class="form-label mt-2">Email</label>
            <input class="form-control" type="email" name="email" value="${safe(editing?.email, "")}" required>
            <label class="form-label mt-2">Password ${editing ? "(opcional)" : ""}</label>
            <input class="form-control" type="password" name="password" ${editing ? "" : "required"} value="">
            <label class="form-label mt-2">Rol</label>
            <select class="form-select" name="rol"><option value="USER" ${String(editing?.rol || "USER") === "USER" ? "selected" : ""}>USER</option><option value="ADMIN" ${String(editing?.rol || "USER") === "ADMIN" ? "selected" : ""}>ADMIN</option></select>
            <label class="form-label mt-2">Monedas acumuladas</label>
            <input class="form-control" type="number" name="monedasAcumuladas" min="0" value="${safe(editing?.monedasAcumuladas, 0)}">
            <label class="form-label mt-2">SteamID</label>
            <input class="form-control" name="steamId" value="${safe(editing?.steamId, "")}">
            <label class="form-label mt-2">Steam persona</label>
            <input class="form-control" name="steamPersonaName" value="${safe(editing?.steamPersonaName, "")}">
            ${adminFormActions("usuarios")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="card border-0 shadow-sm p-3">
            <table class="table align-middle">
              <thead><tr><th>ID</th><th>Nombre</th><th>Email</th><th>Rol</th><th>Monedas</th><th></th></tr></thead>
              <tbody>${state.users.map((u) => `<tr><td>${safe(u.id)}</td><td>${safe(u.nombre)}</td><td>${safe(u.email)}</td><td>${safe(u.rol, "USER")}</td><td>${safe(u.monedasAcumuladas, 0)}</td><td class="text-end d-flex gap-2 justify-content-end">${adminRowActions("usuarios", u)}</td></tr>`).join("")}</tbody>
            </table>
          </div>
        </div>
      </div>
    `
  );
}

function renderAdminProductos() {
  const editing = isEditingResource("productos") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Productos${adminEditBadge("productos")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminProductosForm" data-resource="productos" data-id="${safe(editing?.id || "")}">
            <h3 class="h6">${editing ? "Editar producto" : "Nuevo producto"}</h3>
            <label class="form-label mt-2">Nombre</label>
            <input class="form-control" name="nombre" value="${safe(editing?.nombre, "")}" required>
            <label class="form-label mt-2">Descripcion</label>
            <textarea class="form-control" name="descripcion" rows="3" required>${safe(editing?.descripcion, "")}</textarea>
            <label class="form-label mt-2">Stock</label>
            <input class="form-control" type="number" name="stock" min="0" value="${safe(editing?.stock, 0)}" required>
            <label class="form-label mt-2">Costo monedas</label>
            <input class="form-control" type="number" name="costoMonedas" min="0" value="${safe(editing?.costoMonedas, 0)}" required>
            ${adminFormActions("productos")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="row g-3">${state.products.map((p) => `<div class="col-md-6"><div class="card border-0 shadow-sm p-3 h-100"><div class="d-flex justify-content-between align-items-start"><div><h3 class="h6 mb-1">${safe(p.nombre)}</h3><div class="small text-secondary">Stock ${safe(p.stock, 0)}</div></div><strong>${points(p.costoMonedas)}</strong></div><p class="text-secondary small mt-2 mb-3">${safe(p.descripcion)}</p><div class="d-flex gap-2">${adminRowActions("productos", p)}</div></div></div>`).join("")}</div>
        </div>
      </div>
    `
  );
}

function renderAdminVideojuegos() {
  const editing = isEditingResource("videojuegos") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Videojuegos${adminEditBadge("videojuegos")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminVideojuegosForm" data-resource="videojuegos" data-id="${safe(editing?.id || "")}">
            <h3 class="h6">${editing ? "Editar videojuego" : "Nuevo videojuego"}</h3>
            <label class="form-label mt-2">Titulo</label>
            <input class="form-control" name="titulo" value="${safe(editing?.titulo, "")}" required>
            <label class="form-label mt-2">Usuario</label>
            <select class="form-select" name="usuarioId" required>
              <option value="">Selecciona usuario</option>
              ${optionTags(state.users, (u) => u.id, (u) => `${u.nombre} (${u.rol || "USER"})`, editing?.usuario?.id)}
            </select>
            <label class="form-label mt-2">Plataforma</label>
            <select class="form-select" name="plataformaId" required>
              <option value="">Selecciona plataforma</option>
              ${optionTags(state.platforms, (p) => p.id, (p) => p.nombre, editing?.plataforma?.id)}
            </select>
            <label class="form-label mt-2">Steam AppID</label>
            <input class="form-control" type="number" name="steamAppId" min="0" value="${safe(editing?.steamAppId, "")}">
            ${adminFormActions("videojuegos")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="row g-3">${state.games.map((g) => `<div class="col-md-6"><div class="card border-0 shadow-sm p-3 h-100"><span class="badge badge-soft mb-2">videojuego</span><h3 class="h6">${safe(g.titulo)}</h3><div class="small text-secondary">${safe(g.usuario?.nombre, "Sin usuario")} · ${safe(g.plataforma?.nombre, "Sin plataforma")}</div><div class="small text-secondary">Steam AppID: ${safe(g.steamAppId, "No asignado")}</div><div class="d-flex gap-2 mt-3">${adminRowActions("videojuegos", g)}</div></div></div>`).join("")}</div>
        </div>
      </div>
    `
  );
}

function renderAdminLogros() {
  const editing = isEditingResource("logros") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Logros${adminEditBadge("logros")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminLogrosForm" data-resource="logros" data-id="${safe(editing?.id || "")}">
            <h3 class="h6">${editing ? "Editar logro" : "Nuevo logro"}</h3>
            <label class="form-label mt-2">Nombre</label>
            <input class="form-control" name="nombre" value="${safe(editing?.nombre, "")}" required>
            <label class="form-label mt-2">Descripcion</label>
            <textarea class="form-control" name="descripcion" rows="3">${safe(editing?.descripcion, "")}</textarea>
            <label class="form-label mt-2">Tipo</label>
            <select class="form-select" name="tipo" required>
              <option value="PLATAFORMA" ${String(editing?.tipo || "PLATAFORMA") === "PLATAFORMA" ? "selected" : ""}>PLATAFORMA</option>
              <option value="APLICACION" ${String(editing?.tipo || "PLATAFORMA") === "APLICACION" ? "selected" : ""}>APLICACION</option>
            </select>
            <label class="form-label mt-2">Valor monedas</label>
            <input class="form-control" type="number" name="valorMonedas" min="0" value="${safe(editing?.valorMonedas, 0)}" required>
            <label class="form-label mt-2">Videojuego</label>
            <select class="form-select" name="videojuegoId" required>
              <option value="">Selecciona videojuego</option>
              ${optionTags(state.games, (g) => g.id, (g) => `${g.titulo} (${g.steamAppId || "sin AppID"})`, editing?.videojuego?.id)}
            </select>
            ${adminFormActions("logros")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="row g-3">${state.achievements.map((a) => `<div class="col-md-6"><div class="card border-0 shadow-sm p-3 h-100"><div class="d-flex justify-content-between"><h3 class="h6 mb-0">${safe(a.nombre)}</h3><strong>${points(a.valorMonedas)}</strong></div><div class="small text-secondary mt-1">${safe(a.tipo)}</div><p class="text-secondary small mt-2 mb-3">${safe(a.descripcion, "Sin descripcion")}</p><div class="d-flex gap-2">${adminRowActions("logros", a)}</div></div></div>`).join("")}</div>
        </div>
      </div>
    `
  );
}

function renderAdminCanjes() {
  return adminLayout(
    "Admin Canjes",
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminCanjesForm" data-resource="canjes">
            <h3 class="h6">Nuevo canje</h3>
            <label class="form-label mt-2">Usuario</label>
            <select class="form-select" name="usuarioId" required>
              <option value="">Selecciona usuario</option>
              ${optionTags(state.users, (u) => u.id, (u) => `${u.nombre} (${u.rol || "USER"})`)}
            </select>
            <label class="form-label mt-2">Producto</label>
            <select class="form-select" name="productoId" required>
              <option value="">Selecciona producto</option>
              ${optionTags(state.products, (p) => p.id, (p) => `${p.nombre} - ${p.costoMonedas || 0}`)}
            </select>
            <label class="form-label mt-2">Cantidad</label>
            <input class="form-control" type="number" name="cantidad" min="1" value="1" required>
            <button class="btn btn-primary mt-3" type="submit">Crear canje</button>
          </form>
        </div>
        <div class="col-lg-8">
          <div class="card border-0 shadow-sm p-3">
            <table class="table align-middle">
              <thead><tr><th>ID</th><th>Usuario</th><th>Producto</th><th>Total</th><th></th></tr></thead>
              <tbody>${state.orders.map((o) => `<tr><td>${safe(o.id)}</td><td>${safe(o.usuario?.nombre)}</td><td>${safe(o.producto?.nombre)}</td><td>${points(o.totalMonedas)}</td><td class="text-end"><button class="btn btn-sm btn-outline-danger" data-action="admin-delete" data-resource="canjes" data-id="${o.id}">Borrar</button></td></tr>`).join("")}</tbody>
            </table>
          </div>
        </div>
      </div>
    `
  );
}

function renderAdminPlataformas() {
  const editing = isEditingResource("plataformas") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Plataformas${adminEditBadge("plataformas")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminPlataformasForm" data-resource="plataformas" data-id="${safe(editing?.id || "")}">
            <h3 class="h6">${editing ? "Editar plataforma" : "Nueva plataforma"}</h3>
            <label class="form-label mt-2">Nombre</label>
            <input class="form-control" name="nombre" value="${safe(editing?.nombre, "")}" required>
            ${adminFormActions("plataformas")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="card border-0 shadow-sm p-3"><ul class="list-group list-group-flush">${state.platforms.map((p) => `<li class="list-group-item d-flex justify-content-between align-items-center"><span>${safe(p.nombre)}</span><span class="d-flex gap-2">${adminRowActions("plataformas", p)}</span></li>`).join("")}</ul></div>
        </div>
      </div>
    `
  );
}

function renderAdminConfig() {
  return adminLayout(
    "Admin Configuracion",
    `
      <div class="alert alert-secondary border-0">Ajustes de sistema disponibles.</div>
      <div class="card border-0 shadow-sm p-3">
        <div class="row g-3">
          <div class="col-md-4"><div class="kpi-card"><span>Usuarios</span><strong>${state.users.length}</strong></div></div>
          <div class="col-md-4"><div class="kpi-card"><span>Productos</span><strong>${state.products.length}</strong></div></div>
          <div class="col-md-4"><div class="kpi-card"><span>Logros Steam</span><strong>${state.achievements.length}</strong></div></div>
        </div>
      </div>
    `
  );
}
function adminLayout(title, body) {
  return shell(
    title,
    `
      <div class="card border-0 shadow-sm p-3 mb-3">
        <nav class="nav gap-2">
          <a class="btn btn-outline-primary btn-sm" href="/admin/dashboard" data-link>Dashboard</a>
          <a class="btn btn-outline-primary btn-sm" href="/admin/usuarios" data-link>Usuarios</a>
          <a class="btn btn-outline-primary btn-sm" href="/admin/productos" data-link>Productos</a>
          <a class="btn btn-outline-primary btn-sm" href="/admin/videojuegos" data-link>Videojuegos</a>
          <a class="btn btn-outline-primary btn-sm" href="/admin/logros" data-link>Logros</a>
          <a class="btn btn-outline-primary btn-sm" href="/admin/canjes" data-link>Canjes</a>
          <a class="btn btn-outline-primary btn-sm" href="/admin/plataformas" data-link>Plataformas</a>
          <a class="btn btn-outline-primary btn-sm" href="/admin/configuracion" data-link>Configuracion</a>
        </nav>
      </div>
      ${body}
    `
  );
}

function renderDetail(path) {
  const m = path.match(/^\/detalle\/(product|game)\/(\d+)$/);
  if (!m) return shell("404", `<div class="alert alert-danger border-0">Ruta no encontrada.</div>`);

  const [, kind, id] = m;
  if (kind === "product") {
    const p = state.products.find((x) => String(x.id) === id);
    if (!p) return shell("Detalle", `<div class="alert alert-secondary border-0">Producto no encontrado.</div>`);
    return shell(
      "Detalle producto",
      `<div class="card border-0 shadow-sm p-4"><p><strong>Nombre:</strong> ${safe(p.nombre)}</p><p><strong>Descripcion:</strong> ${safe(p.descripcion)}</p><p><strong>Stock:</strong> ${safe(p.stock, 0)}</p><p><strong>Costo:</strong> ${points(p.costoMonedas)}</p></div>`
    );
  }

  const g = state.games.find((x) => String(x.id) === id);
  if (!g) return shell("Detalle", `<div class="alert alert-secondary border-0">Videojuego no encontrado.</div>`);
  return shell(
    "Detalle videojuego",
    `<div class="card border-0 shadow-sm p-4"><p><strong>Titulo:</strong> ${safe(g.titulo)}</p><p><strong>Plataforma:</strong> ${safe(g.plataforma?.nombre)}</p><p><strong>Usuario:</strong> ${safe(g.usuario?.nombre)}</p><p><strong>Steam AppID:</strong> ${safe(g.steamAppId, "No asignado")}</p></div>`
  );
}

function guardPath(path) {
  if (path === "/catalago") return "/catalogo";
  if (path === "/usuario") return "/usuario/dashboard";
  if (path === "/admin") return "/admin/dashboard";

  if (GUEST_ROUTES.has(path) || /^\/detalle\/(product|game)\/\d+$/.test(path)) return path;

  if (USER_ROUTES.has(path)) {
    if (!state.sessionUser) return "/acceso";
    return path;
  }

  if (ADMIN_ROUTES.has(path)) {
    if (!state.sessionUser) return "/acceso";
    if (!isAdmin()) return "/usuario/dashboard";
    return path;
  }

  return "/";
}

function route() {
  const requested = window.location.pathname;
  const path = guardPath(requested);

  if (path !== requested) {
    window.history.replaceState({}, "", path);
  }

  if (path === "/") {
    app.innerHTML = renderHome();
    return;
  }
  if (path === "/catalogo") {
    app.innerHTML = renderCatalogo();
    return;
  }
  if (path === "/acceso") {
    app.innerHTML = renderAcceso();
    return;
  }
  if (path === "/registro") {
    app.innerHTML = renderRegistro();
    return;
  }
  if (path === "/olvide-password") {
    app.innerHTML = renderForgot();
    return;
  }
  if (/^\/detalle\/(product|game)\/\d+$/.test(path)) {
    app.innerHTML = renderDetail(path);
    return;
  }

  if (path === "/usuario/dashboard") app.innerHTML = renderUserDashboard();
  else if (path === "/usuario/perfil") app.innerHTML = renderUserPerfil();
  else if (path === "/usuario/carrito") app.innerHTML = renderUserCarrito();
  else if (path === "/usuario/canjes") app.innerHTML = renderUserCanjes();
  else if (path === "/usuario/logros") app.innerHTML = renderUserLogros();
  else if (path === "/usuario/configuracion") app.innerHTML = renderUserConfig();
  else if (path === "/admin/dashboard") app.innerHTML = renderAdminDashboard();
  else if (path === "/admin/usuarios") app.innerHTML = renderAdminUsuarios();
  else if (path === "/admin/productos") app.innerHTML = renderAdminProductos();
  else if (path === "/admin/videojuegos") app.innerHTML = renderAdminVideojuegos();
  else if (path === "/admin/logros") app.innerHTML = renderAdminLogros();
  else if (path === "/admin/canjes") app.innerHTML = renderAdminCanjes();
  else if (path === "/admin/plataformas") app.innerHTML = renderAdminPlataformas();
  else if (path === "/admin/configuracion") app.innerHTML = renderAdminConfig();
  else app.innerHTML = shell("404", `<div class="alert alert-danger border-0">Ruta no encontrada.</div>`);
}

async function bootstrap() {
  app.innerHTML = shell("Cargando", `<div class="alert alert-info border-0">Inicializando...</div>`);
  await fetchSessionUser();
  await loadData();
  await loadSteamData();
  route();
}

window.addEventListener("popstate", route);

document.addEventListener("click", async (event) => {
  const link = event.target.closest("a[data-link]");
  if (link) {
    event.preventDefault();
    navigate(link.getAttribute("href"));
    return;
  }

  const action = event.target.closest("[data-action]");
  if (!action) return;

  const name = action.dataset.action;

  if (name === "admin-cancel") {
    cancelAdminEdit();
    return;
  }

  if (name === "admin-edit") {
    const resource = action.dataset.resource;
    const item = findAdminItem(resource, action.dataset.id);
    if (item) startAdminEdit(resource, action.dataset.id, item);
    return;
  }

  if (name === "admin-delete") {
    const resource = action.dataset.resource;
    const id = action.dataset.id;
    if (!confirm(`Seguro que quieres borrar este elemento de ${resource}?`)) return;

    try {
      await apiDelete(`/api/${resource}/${id}`);
      if (state.adminEdit && String(state.adminEdit.resource) === String(resource) && String(state.adminEdit.id) === String(id)) {
        cancelAdminEdit();
      }
      await loadData();
      route();
    } catch (e) {
      alert(e.message || "No se pudo borrar");
    }
    return;
  }

  if (name === "detail-product") {
    navigate(`/detalle/product/${action.dataset.id}`);
    return;
  }
  if (name === "detail-game") {
    navigate(`/detalle/game/${action.dataset.id}`);
    return;
  }

  if (name === "add-cart") {
    const id = action.dataset.id;
    if (!state.cart.includes(id)) state.cart.push(id);
    action.disabled = true;
    action.textContent = "Anadido";
    return;
  }

  if (name === "remove-cart") {
    const id = action.dataset.id;
    state.cart = state.cart.filter((x) => String(x) !== String(id));
    route();
    return;
  }

  if (name === "confirm-cart") {
    if (!state.sessionUser || !state.cart.length) return;

    action.disabled = true;
    action.textContent = "Procesando...";

    try {
      for (const productId of state.cart) {
        await apiPost("/api/canjes", {
          usuarioId: state.sessionUser.id,
          productoId: Number(productId),
          cantidad: 1
        });
      }
      state.cart = [];
      await loadData();
      navigate("/usuario/canjes");
    } catch (e) {
      alert(`No se pudo completar el canje: ${e.message}`);
      action.disabled = false;
      action.textContent = "Confirmar canje";
    }
    return;
  }

  if (name === "sync-steam") {
    if (!state.sessionUser) return;

    action.disabled = true;
    action.textContent = "Sincronizando...";
    try {
      state.steamSyncResult = await apiPost("/api/steam/sync", {});
      await fetchSessionUser();
      await loadData();
      await loadSteamData();
      route();
    } catch (e) {
      alert(e.message || "No se pudo sincronizar Steam");
      action.disabled = false;
      action.textContent = "Sincronizar logros Steam";
    }
    return;
  }

  if (name === "logout") {
    event.preventDefault();
    try {
      await apiPost("/api/auth/logout", {});
    } catch {
      // ignore
    }
    state.sessionUser = null;
    state.cart = [];
    navigate("/");
  }
});

document.addEventListener("submit", async (event) => {
  const adminForm = event.target.closest("form[data-resource]");
  if (adminForm) {
    event.preventDefault();
    const resource = adminForm.dataset.resource;
    const id = adminForm.dataset.id;
    const form = new FormData(adminForm);

    try {
      let payload;

      if (resource === "usuarios") {
        payload = {
          nombre: form.get("nombre"),
          email: form.get("email"),
          password: form.get("password") || undefined,
          rol: form.get("rol"),
          monedasAcumuladas: Number(form.get("monedasAcumuladas") || 0),
          steamId: form.get("steamId") || null,
          steamPersonaName: form.get("steamPersonaName") || null
        };
      } else if (resource === "productos") {
        payload = {
          nombre: form.get("nombre"),
          descripcion: form.get("descripcion"),
          stock: Number(form.get("stock") || 0),
          costoMonedas: Number(form.get("costoMonedas") || 0)
        };
      } else if (resource === "plataformas") {
        payload = { nombre: form.get("nombre") };
      } else if (resource === "videojuegos") {
        payload = {
          titulo: form.get("titulo"),
          usuarioId: Number(form.get("usuarioId") || 0),
          plataformaId: Number(form.get("plataformaId") || 0),
          steamAppId: form.get("steamAppId") ? Number(form.get("steamAppId")) : null
        };
      } else if (resource === "logros") {
        payload = {
          nombre: form.get("nombre"),
          descripcion: form.get("descripcion"),
          tipo: form.get("tipo"),
          valorMonedas: Number(form.get("valorMonedas") || 0),
          videojuegoId: Number(form.get("videojuegoId") || 0)
        };
      } else if (resource === "canjes") {
        payload = {
          usuarioId: Number(form.get("usuarioId") || 0),
          productoId: Number(form.get("productoId") || 0),
          cantidad: Number(form.get("cantidad") || 1)
        };
      }

      if (resource === "canjes") {
        await apiPost(`/api/${resource}`, payload);
      } else if (id) {
        await apiPut(`/api/${resource}/${id}`, payload);
      } else {
        await apiPost(`/api/${resource}`, payload);
      }

      if (state.adminEdit && state.adminEdit.resource === resource) cancelAdminEdit();
      await loadData();
      route();
    } catch (e) {
      alert(e.message || `No se pudo guardar ${resource}`);
    }
    return;
  }

  if (event.target.id === "loginForm") {
    event.preventDefault();
    const form = new FormData(event.target);

    try {
      const user = await apiPost("/api/auth/login", {
        email: form.get("email"),
        password: form.get("password")
      });
      state.sessionUser = user;
      await loadData();
      await loadSteamData();
      navigate(isAdmin() ? "/admin/dashboard" : "/usuario/dashboard");
    } catch (e) {
      alert(e.message || "No se pudo iniciar sesion");
    }
    return;
  }

  if (event.target.id === "registerForm") {
    event.preventDefault();
    const form = new FormData(event.target);

    try {
      await apiPost("/api/auth/register", {
        nombre: form.get("nombre"),
        email: form.get("email"),
        password: form.get("password")
      });
      alert("Registro completado. Ya puedes iniciar sesion.");
      navigate("/acceso");
    } catch (e) {
      alert(e.message || "No se pudo registrar");
    }
  }
});

document.addEventListener("input", (event) => {
  if (event.target.id === "steamGameSearch") {
    state.steamSearch = event.target.value || "";
    route();
  }
});

window.addEventListener("error", (event) => {
  showFatalError(event.error?.message || event.message || "Error inesperado en el frontend");
});

window.addEventListener("unhandledrejection", (event) => {
  showFatalError(event.reason?.message || String(event.reason || "Error inesperado en el frontend"));
});

bootstrap().catch((error) => {
  showFatalError(error?.message || "Error inesperado al iniciar la interfaz");
});
