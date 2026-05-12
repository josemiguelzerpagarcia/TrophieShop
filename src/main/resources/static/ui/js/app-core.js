export const state = {
  sessionUser: null,
  users: [],
  products: [],
  achievements: [],
  unlockedAchievements: [],
  orders: [],
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

export const GUEST_ROUTES = new Set(["/", "/catalogo", "/acceso", "/registro", "/olvide-password"]);
export const USER_ROUTES = new Set([
  "/usuario/dashboard",
  "/usuario/perfil",
  "/usuario/carrito",
  "/usuario/canjes",
  "/usuario/logros",
  "/usuario/configuracion"
]);
export const ADMIN_ROUTES = new Set([
  "/admin/dashboard",
  "/admin/usuarios",
  "/admin/productos",
  "/admin/logros",
  "/admin/canjes",
  "/admin/configuracion"
]);

export const safe = (value, fallback = "-") => (value === null || value === undefined || value === "" ? fallback : value);
export const points = (value) => `${Number(value || 0).toLocaleString("es-ES")} monedas`;

export function isAdmin() {
  return state.sessionUser && String(state.sessionUser.rol).toUpperCase() === "ADMIN";
}

export function getInitial(name) {
  if (!name) return "U";
  return String(name).trim().charAt(0).toUpperCase();
}

export function startAdminEdit(resource, id, item) {
  state.adminEdit = { resource, id, item };
}

export function cancelAdminEdit() {
  state.adminEdit = null;
}

export function isEditingResource(resource) {
  return Boolean(state.adminEdit && state.adminEdit.resource === resource);
}

export function getAdminEditing(resource, id) {
  if (!state.adminEdit || state.adminEdit.resource !== resource) return null;
  return String(state.adminEdit.id) === String(id) ? state.adminEdit.item : null;
}

async function parseErrorResponse(response, fallback) {
  let message = fallback;
  try {
    const payload = await response.json();
    message = payload.error || message;
  } catch {
    const text = await response.text();
    if (text) message = text;
  }
  return message;
}

export async function apiGet(url) {
  const response = await fetch(url, { credentials: "same-origin" });
  if (!response.ok) {
    throw new Error(await parseErrorResponse(response, `Error ${response.status} en ${url}`));
  }
  return response.json();
}

export async function apiPost(url, body) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "same-origin",
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    throw new Error(await parseErrorResponse(response, `Error ${response.status}`));
  }

  return response.json();
}

export async function apiPut(url, body) {
  const response = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    credentials: "same-origin",
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    throw new Error(await parseErrorResponse(response, `Error ${response.status}`));
  }

  return response.json();
}

export async function apiDelete(url) {
  const response = await fetch(url, {
    method: "DELETE",
    credentials: "same-origin"
  });

  if (!response.ok) {
    throw new Error(await parseErrorResponse(response, `Error ${response.status}`));
  }

  if (response.status === 204 || response.status === 205) return null;

  try {
    return await response.json();
  } catch {
    return null;
  }
}

export async function fetchSessionUser() {
  try {
    state.sessionUser = await apiGet("/api/auth/me");
  } catch {
    state.sessionUser = null;
  }
}

export async function loadSteamData() {
  state.steamProfile = null;
  state.steamLibrary = [];
  state.steamError = "";

  if (!state.sessionUser) return;

  try {
    state.steamProfile = await apiGet("/api/steam/me");
  } catch (error) {
    state.steamError = error.message || "No se pudo leer el perfil de Steam";
    state.steamProfile = null;
  }

  try {
    const library = await apiGet("/api/steam/library");
    state.steamLibrary = Array.isArray(library) ? library : [];
  } catch (error) {
    state.steamError = error.message || "No se pudo leer la biblioteca de Steam";
    state.steamLibrary = [];
  }
}

export async function loadData() {
  state.loading = true;
  state.error = "";

  try {
    const requests = [
      apiGet("/api/usuarios"),
      apiGet("/api/productos"),
      apiGet("/api/logros"),
      apiGet("/api/logros-desbloqueados"),
      apiGet("/api/canjes")
    ];

    const [users, products, achievements, unlocked, orders] = await Promise.allSettled(requests);

    state.users = users.status === "fulfilled" && Array.isArray(users.value) ? users.value : [];
    state.products = products.status === "fulfilled" && Array.isArray(products.value) ? products.value : [];
    state.achievements = achievements.status === "fulfilled" && Array.isArray(achievements.value) ? achievements.value : [];
    state.unlockedAchievements = unlocked.status === "fulfilled" && Array.isArray(unlocked.value) ? unlocked.value : [];
    state.orders = orders.status === "fulfilled" && Array.isArray(orders.value) ? orders.value : [];

    const allFailed = [users, products, achievements, unlocked, orders]
      .every((result) => result.status === "rejected");
    if (allFailed) {
      state.error = "No se pudo conectar con la API";
    }
  } finally {
    state.loading = false;
  }
}