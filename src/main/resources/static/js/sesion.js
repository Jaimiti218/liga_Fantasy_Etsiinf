// ─── Obtener usuario actual del servidor ──────────────────────────────────────
async function obtenerUsuarioActual() {
    try {
        const res = await fetch('/users/me', { credentials: 'include' });
        if (!res.ok) return null;
        return await res.json(); // { id, username, role }
    } catch (e) {
        return null;
    }
}

// ─── Cerrar sesión ────────────────────────────────────────────────────────────
async function cerrarSesionCompleta() {
    if (!confirm('¿Seguro que quieres cerrar sesión?')) return;
    
    try {
        await fetch('/logout', { 
            method: 'POST',
            credentials: 'include'
            // ya no hace falta el X-XSRF-TOKEN
        });
    } catch (e) {}
    
    localStorage.removeItem('username');
    window.location.href = '/';
}

// ─── Obtener token CSRF de la cookie ─────────────────────────────────────────
function obtenerCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : '';
}