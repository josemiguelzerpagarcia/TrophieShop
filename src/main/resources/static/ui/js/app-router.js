import { apiDelete, apiPost, apiPut, cancelAdminEdit, fetchSessionUser, loadData, loadSteamData, isAdmin, state, GUEST_ROUTES, USER_ROUTES, ADMIN_ROUTES, startAdminEdit, safe } from "./app-core.js";
import { adminLayout, findAdminItem, renderAcceso, renderAdminCanjes, renderAdminConfig, renderAdminDashboard, renderAdminLogros, renderAdminPlataformas, renderAdminProductos, renderAdminUsuarios, renderAdminVideojuegos, renderCatalogo, renderDetail, renderForgot, renderHome, renderRegistro, renderUserCanjes, renderUserCarrito, renderUserConfig, renderUserDashboard, renderUserLogros, renderUserPerfil } from "./app-renderers.js";

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

function navigate(path) {
  if (window.location.pathname !== path) {
    window.history.pushState({}, "", path);
  }
  route();
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
    if (state.sessionUser) {
      navigate(isAdmin() ? "/admin/dashboard" : "/usuario/dashboard");
      return;
    }
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
  else app.innerHTML = adminLayout("404", `<div class="alert alert-danger border-0">Ruta no encontrada.</div>`);
}

async function bootstrap() {
  app.innerHTML = renderHome();
  await fetchSessionUser();
  await loadData();
  await loadSteamData();

  if (state.sessionUser && window.location.pathname === "/acceso") {
    navigate(isAdmin() ? "/admin/dashboard" : "/usuario/dashboard");
    return;
  }

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
    route();
    return;
  }

  if (name === "admin-edit") {
    const resource = action.dataset.resource;
    const item = findAdminItem(resource, action.dataset.id);
    if (item) {
      startAdminEdit(resource, action.dataset.id, item);
      route();
    }
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
    } catch (error) {
      alert(error.message || "No se pudo borrar");
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
    state.cart = state.cart.filter((item) => String(item) !== String(id));
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
    } catch (error) {
      alert(`No se pudo completar el canje: ${error.message}`);
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
    } catch (error) {
      alert(error.message || "No se pudo sincronizar Steam");
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
    const rawId = adminForm.dataset.id;
    const id = rawId && rawId !== "-" ? rawId : "";
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
    } catch (error) {
      alert(error.message || `No se pudo guardar ${resource}`);
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
    } catch (error) {
      alert(error.message || "No se pudo iniciar sesion");
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
    } catch (error) {
      alert(error.message || "No se pudo registrar");
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

export async function initApp() {
  app.innerHTML = renderHome();
  await bootstrap();
}
