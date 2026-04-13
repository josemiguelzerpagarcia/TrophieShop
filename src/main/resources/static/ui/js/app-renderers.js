import { state, safe, points, isAdmin, getInitial, isEditingResource } from "./app-core.js";

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

export function shell(title, body) {
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

function productCard(product) {
  return `
    <article class="col-md-6 col-xl-4">
      <div class="card h-100 product-card border-0 shadow-sm">
        <div class="card-body d-flex flex-column">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="badge badge-soft">producto</span>
            <span class="small text-secondary">Stock: ${safe(product.stock, 0)}</span>
          </div>
          <h5 class="card-title">${safe(product.nombre)}</h5>
          <p class="card-text text-secondary">${safe(product.descripcion, "Sin descripcion")}</p>
          <div class="mt-auto d-flex justify-content-between align-items-end">
            <div class="fw-bold">${points(product.costoMonedas)}</div>
            <div class="d-flex gap-2">
              <button class="btn btn-outline-primary btn-sm" data-action="detail-product" data-id="${product.id}">Detalle</button>
              ${state.sessionUser ? `<button class="btn btn-primary btn-sm" data-action="add-cart" data-id="${product.id}">Canjear</button>` : ""}
            </div>
          </div>
        </div>
      </div>
    </article>
  `;
}

function gameCard(game) {
  return `
    <article class="col-md-6 col-xl-4">
      <div class="card h-100 product-card border-0 shadow-sm">
        <div class="card-body d-flex flex-column">
          <span class="badge badge-soft mb-2">videojuego</span>
          <h5 class="card-title">${safe(game.titulo)}</h5>
          <p class="card-text text-secondary">Plataforma: ${safe(game.plataforma?.nombre, "N/A")}</p>
          <p class="card-text text-secondary">Usuario: ${safe(game.usuario?.nombre, "N/A")}</p>
          <p class="card-text text-secondary">Steam AppID: ${safe(game.steamAppId, "No asignado")}</p>
          <div class="mt-auto d-flex justify-content-end">
            <button class="btn btn-outline-primary btn-sm" data-action="detail-game" data-id="${game.id}">Detalle</button>
          </div>
        </div>
      </div>
    </article>
  `;
}

export function renderHome() {
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

export function renderCatalogo() {
  const productsHtml = state.products.map(productCard).join("");
  return shell(
    "Catalogo de merchandising",
    `
      <div class="row g-3">${productsHtml || `<div class="col-12"><div class="alert alert-secondary border-0">No hay productos.</div></div>`}</div>
    `
  );
}

export function renderAcceso() {
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

export function renderRegistro() {
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

export function renderForgot() {
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

export function renderUserDashboard() {
  return userLayout(
    "Dashboard de usuario",
    `<div class="row g-3 mb-3"><div class="col-md-4"><div class="kpi-card h-100"><span>Monedas</span><strong>${points(state.sessionUser?.monedasAcumuladas)}</strong></div></div><div class="col-md-4"><div class="kpi-card h-100"><span>Canjes</span><strong>${state.orders.length}</strong></div></div><div class="col-md-4"><div class="kpi-card h-100"><span>Logros</span><strong>${state.achievements.length}</strong></div></div></div>`
  );
}

export function renderUserPerfil() {
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

export function renderUserCarrito() {
  const items = state.cart
    .map((id) => state.products.find((product) => String(product.id) === String(id)))
    .filter(Boolean);

  if (!items.length) {
    return userLayout("Carrito", `<div class="alert alert-secondary border-0">No hay productos en el carrito.</div>`);
  }

  const total = items.reduce((sum, item) => sum + Number(item.costoMonedas || 0), 0);
  return userLayout(
    "Carrito",
    `<div class="card border-0 shadow-sm p-3"><table class="table"><thead><tr><th>Producto</th><th>Monedas</th><th></th></tr></thead><tbody>${items.map((item) => `<tr><td>${safe(item.nombre)}</td><td>${points(item.costoMonedas)}</td><td class="text-end"><button class="btn btn-sm btn-outline-danger" data-action="remove-cart" data-id="${item.id}">Quitar</button></td></tr>`).join("")}</tbody></table><div class="d-flex justify-content-between"><strong>Total: ${points(total)}</strong><button class="btn btn-primary" data-action="confirm-cart">Confirmar canje</button></div></div>`
  );
}

export function renderUserCanjes() {
  return userLayout(
    "Canjes",
    `<div class="card border-0 shadow-sm p-3"><table class="table"><thead><tr><th>ID</th><th>Producto</th><th>Total</th><th>Fecha</th></tr></thead><tbody>${state.orders.map((order) => `<tr><td>${safe(order.id)}</td><td>${safe(order.producto?.nombre)}</td><td>${points(order.totalMonedas)}</td><td>${safe(order.fechaCanje)}</td></tr>`).join("") || `<tr><td colspan="4" class="text-secondary">No hay canjes.</td></tr>`}</tbody></table></div>`
  );
}

export function renderUserLogros() {
  const normalizedSearch = String(state.steamSearch || "").trim().toLowerCase();
  const filteredLibrary = normalizedSearch
    ? state.steamLibrary.filter((game) => String(game.name || "").toLowerCase().includes(normalizedSearch))
    : state.steamLibrary;

  const totalUnlocked = filteredLibrary.reduce((sum, game) => sum + Number(game.unlockedCount || 0), 0);
  const totalProjectedPoints = filteredLibrary.reduce((sum, game) => sum + Number(game.totalPoints || 0), 0);

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
    ? `<div class="alert alert-warning border-0">${safe(state.steamError)}. Revisa la vinculación de Steam y vuelve a intentarlo.</div>`
    : "";

  const searchBlock = `
    <div class="card border-0 shadow-sm p-3 mb-3">
      <label class="form-label mb-2" for="steamGameSearch">Buscar juego por nombre</label>
      <input id="steamGameSearch" class="form-control" type="search" placeholder="Ej: Counter, GTA, Elden..." value="${safe(state.steamSearch, "")}">
    </div>
  `;

  const libraryPreview = filteredLibrary.length
    ? `<div class="card border-0 shadow-sm p-3 mb-3"><h3 class="h6 mb-2">Resumen Steam</h3><p class="mb-2">Puntos potenciales por logros visibles: <strong>${points(totalProjectedPoints)}</strong></p><div class="small text-secondary">Puntos por logro: comun = 200, raro = 500</div></div>
       <div class="steam-library-grid">${filteredLibrary.map((game) => `<article class="steam-game-card"><img class="steam-game-image" src="https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${safe(game.appId, 0)}/header.jpg" alt="${safe(game.name, "Juego Steam")}" loading="lazy" onerror="this.src='https://placehold.co/460x215/F2F2F2/123859?text=Sin+portada'" /><div class="steam-game-body"><div class="d-flex justify-content-between align-items-start gap-2 mb-2"><h3 class="h6 mb-0">${safe(game.name, "App " + safe(game.appId))}</h3><span class="small text-secondary">AppID ${safe(game.appId)}</span></div><div class="small text-secondary mb-2">Tiempo jugado: ${safe(game.playtimeMinutes, 0)} min</div><div class="steam-points-row mb-2"><span class="badge text-bg-light">Logros: ${safe(game.unlockedCount, 0)}</span><span class="badge text-bg-light">${points(game.totalPoints)}</span></div><div class="steam-achievements-list">${(Array.isArray(game.achievements) ? game.achievements : []).map((achievement) => `<div class="steam-achievement-item"><div><strong>${safe(achievement.displayName, achievement.apiName)}</strong><div class="small text-secondary">${achievement.rarityType === "RARE" ? "Raro" : "Comun"} · ${safe(achievement.rarityPercent, 100)}%</div></div><span class="steam-achievement-points">${points(achievement.points)}</span></div>`).join("") || `<div class="small text-secondary">Este juego no muestra logros desbloqueados para este usuario.</div>`}</div></div></article>`).join("")}</div>`
    : state.steamLibrary.length
      ? `<div class="alert alert-secondary border-0">No hay juegos que coincidan con "${safe(state.steamSearch, "")}".</div>`
      : `<div class="alert alert-secondary border-0">Steam no devolvió juegos para esta cuenta.</div>`;

  return userLayout(
    "Logros",
    `${steamStatus}${steamError}${syncResult}${searchBlock}${libraryPreview}`
  );
}

export function renderUserConfig() {
  return userLayout("Configuracion", `<div class="alert alert-secondary border-0">Configuracion disponible.</div>`);
}

export function optionTags(items, valueGetter, labelGetter, selectedValue) {
  return items
    .map((item) => {
      const value = valueGetter(item);
      const selected = String(value) === String(selectedValue) ? "selected" : "";
      return `<option value="${safe(value)}" ${selected}>${safe(labelGetter(item))}</option>`;
    })
    .join("");
}

export function adminEditBadge(resource) {
  return isEditingResource(resource)
    ? `<span class="badge text-bg-warning ms-2">Editando</span>`
    : "";
}

export function adminFormActions(resource) {
  return `
    <div class="d-flex gap-2 mt-3">
      <button class="btn btn-primary" type="submit">${isEditingResource(resource) ? "Guardar cambios" : "Crear"}</button>
      ${isEditingResource(resource) ? `<button class="btn btn-outline-secondary" type="button" data-action="admin-cancel">Cancelar</button>` : ""}
    </div>
  `;
}

export function adminRowActions(resource, item) {
  return `
    <button class="btn btn-sm btn-outline-primary" data-action="admin-edit" data-resource="${resource}" data-id="${item.id}">Editar</button>
    <button class="btn btn-sm btn-outline-danger" data-action="admin-delete" data-resource="${resource}" data-id="${item.id}">Borrar</button>
  `;
}

export function findAdminItem(resource, id) {
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

export function adminLayout(title, body) {
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

export function renderAdminDashboard() {
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

export function renderAdminUsuarios() {
  const editing = isEditingResource("usuarios") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Usuarios${adminEditBadge("usuarios")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminUsuariosForm" data-resource="usuarios" data-id="${editing?.id ?? ""}">
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
              <tbody>${state.users.map((user) => `<tr><td>${safe(user.id)}</td><td>${safe(user.nombre)}</td><td>${safe(user.email)}</td><td>${safe(user.rol, "USER")}</td><td>${safe(user.monedasAcumuladas, 0)}</td><td class="text-end d-flex gap-2 justify-content-end">${adminRowActions("usuarios", user)}</td></tr>`).join("")}</tbody>
            </table>
          </div>
        </div>
      </div>
    `
  );
}

export function renderAdminProductos() {
  const editing = isEditingResource("productos") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Productos${adminEditBadge("productos")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminProductosForm" data-resource="productos" data-id="${editing?.id ?? ""}">
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
          <div class="row g-3">${state.products.map((product) => `<div class="col-md-6"><div class="card border-0 shadow-sm p-3 h-100"><div class="d-flex justify-content-between align-items-start"><div><h3 class="h6 mb-1">${safe(product.nombre)}</h3><div class="small text-secondary">Stock ${safe(product.stock, 0)}</div></div><strong>${points(product.costoMonedas)}</strong></div><p class="text-secondary small mt-2 mb-3">${safe(product.descripcion)}</p><div class="d-flex gap-2">${adminRowActions("productos", product)}</div></div></div>`).join("")}</div>
        </div>
      </div>
    `
  );
}

export function renderAdminVideojuegos() {
  const editing = isEditingResource("videojuegos") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Videojuegos${adminEditBadge("videojuegos")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminVideojuegosForm" data-resource="videojuegos" data-id="${editing?.id ?? ""}">
            <h3 class="h6">${editing ? "Editar videojuego" : "Nuevo videojuego"}</h3>
            <label class="form-label mt-2">Titulo</label>
            <input class="form-control" name="titulo" value="${safe(editing?.titulo, "")}" required>
            <label class="form-label mt-2">Usuario</label>
            <select class="form-select" name="usuarioId" required>
              <option value="">Selecciona usuario</option>
              ${optionTags(state.users, (user) => user.id, (user) => `${user.nombre} (${user.rol || "USER"})`, editing?.usuario?.id)}
            </select>
            <label class="form-label mt-2">Plataforma</label>
            <select class="form-select" name="plataformaId" required>
              <option value="">Selecciona plataforma</option>
              ${optionTags(state.platforms, (platform) => platform.id, (platform) => platform.nombre, editing?.plataforma?.id)}
            </select>
            <label class="form-label mt-2">Steam AppID</label>
            <input class="form-control" type="number" name="steamAppId" min="0" value="${safe(editing?.steamAppId, "")}">
            ${adminFormActions("videojuegos")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="row g-3">${state.games.map((game) => `<div class="col-md-6"><div class="card border-0 shadow-sm p-3 h-100"><span class="badge badge-soft mb-2">videojuego</span><h3 class="h6">${safe(game.titulo)}</h3><div class="small text-secondary">${safe(game.usuario?.nombre, "Sin usuario")} · ${safe(game.plataforma?.nombre, "Sin plataforma")}</div><div class="small text-secondary">Steam AppID: ${safe(game.steamAppId, "No asignado")}</div><div class="d-flex gap-2 mt-3">${adminRowActions("videojuegos", game)}</div></div></div>`).join("")}</div>
        </div>
      </div>
    `
  );
}

export function renderAdminLogros() {
  const editing = isEditingResource("logros") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Logros${adminEditBadge("logros")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminLogrosForm" data-resource="logros" data-id="${editing?.id ?? ""}">
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
              ${optionTags(state.games, (game) => game.id, (game) => `${game.titulo} (${game.steamAppId || "sin AppID"})`, editing?.videojuego?.id)}
            </select>
            ${adminFormActions("logros")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="row g-3">${state.achievements.map((achievement) => `<div class="col-md-6"><div class="card border-0 shadow-sm p-3 h-100"><div class="d-flex justify-content-between"><h3 class="h6 mb-0">${safe(achievement.nombre)}</h3><strong>${points(achievement.valorMonedas)}</strong></div><div class="small text-secondary mt-1">${safe(achievement.tipo)}</div><p class="text-secondary small mt-2 mb-3">${safe(achievement.descripcion, "Sin descripcion")}</p><div class="d-flex gap-2">${adminRowActions("logros", achievement)}</div></div></div>`).join("")}</div>
        </div>
      </div>
    `
  );
}

export function renderAdminCanjes() {
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
              ${optionTags(state.users, (user) => user.id, (user) => `${user.nombre} (${user.rol || "USER"})`)}
            </select>
            <label class="form-label mt-2">Producto</label>
            <select class="form-select" name="productoId" required>
              <option value="">Selecciona producto</option>
              ${optionTags(state.products, (product) => product.id, (product) => `${product.nombre} - ${product.costoMonedas || 0}`)}
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
              <tbody>${state.orders.map((order) => `<tr><td>${safe(order.id)}</td><td>${safe(order.usuario?.nombre)}</td><td>${safe(order.producto?.nombre)}</td><td>${points(order.totalMonedas)}</td><td class="text-end"><button class="btn btn-sm btn-outline-danger" data-action="admin-delete" data-resource="canjes" data-id="${order.id}">Borrar</button></td></tr>`).join("")}</tbody>
            </table>
          </div>
        </div>
      </div>
    `
  );
}

export function renderAdminPlataformas() {
  const editing = isEditingResource("plataformas") ? state.adminEdit.item : null;
  return adminLayout(
    `Admin Plataformas${adminEditBadge("plataformas")}`,
    `
      <div class="row g-4">
        <div class="col-lg-4">
          <form class="card border-0 shadow-sm p-3" id="adminPlataformasForm" data-resource="plataformas" data-id="${editing?.id ?? ""}">
            <h3 class="h6">${editing ? "Editar plataforma" : "Nueva plataforma"}</h3>
            <label class="form-label mt-2">Nombre</label>
            <input class="form-control" name="nombre" value="${safe(editing?.nombre, "")}" required>
            ${adminFormActions("plataformas")}
          </form>
        </div>
        <div class="col-lg-8">
          <div class="card border-0 shadow-sm p-3"><ul class="list-group list-group-flush">${state.platforms.map((platform) => `<li class="list-group-item d-flex justify-content-between align-items-center"><span>${safe(platform.nombre)}</span><span class="d-flex gap-2">${adminRowActions("plataformas", platform)}</span></li>`).join("")}</ul></div>
        </div>
      </div>
    `
  );
}

export function renderAdminConfig() {
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

export function renderDetail(path) {
  const match = path.match(/^\/detalle\/(product|game)\/(\d+)$/);
  if (!match) return shell("404", `<div class="alert alert-danger border-0">Ruta no encontrada.</div>`);

  const [, kind, id] = match;
  if (kind === "product") {
    const product = state.products.find((item) => String(item.id) === id);
    if (!product) return shell("Detalle", `<div class="alert alert-secondary border-0">Producto no encontrado.</div>`);
    return shell(
      "Detalle producto",
      `<div class="card border-0 shadow-sm p-4"><p><strong>Nombre:</strong> ${safe(product.nombre)}</p><p><strong>Descripcion:</strong> ${safe(product.descripcion)}</p><p><strong>Stock:</strong> ${safe(product.stock, 0)}</p><p><strong>Costo:</strong> ${points(product.costoMonedas)}</p></div>`
    );
  }

  const game = state.games.find((item) => String(item.id) === id);
  if (!game) return shell("Detalle", `<div class="alert alert-secondary border-0">Videojuego no encontrado.</div>`);
  return shell(
    "Detalle videojuego",
    `<div class="card border-0 shadow-sm p-4"><p><strong>Titulo:</strong> ${safe(game.titulo)}</p><p><strong>Plataforma:</strong> ${safe(game.plataforma?.nombre)}</p><p><strong>Usuario:</strong> ${safe(game.usuario?.nombre)}</p><p><strong>Steam AppID:</strong> ${safe(game.steamAppId, "No asignado")}</p></div>`
  );
}