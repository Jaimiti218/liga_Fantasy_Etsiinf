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
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': obtenerCsrfToken() }
        });
    } catch (e) {}
    
    // Limpiar localStorage también por si acaso
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    
    window.location.href = '/';
}

// ─── Obtener token CSRF de la cookie ─────────────────────────────────────────
function obtenerCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : '';
}