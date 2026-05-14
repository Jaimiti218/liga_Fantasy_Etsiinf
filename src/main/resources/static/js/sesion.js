// ─── Obtener usuario actual del servidor ──────────────────────────────────────
async function obtenerUsuarioActual() {
    try {
        const res = await fetch('/users/me', { credentials: 'include' });
        if (!res.ok) return null;
        const datos = await res.json();
        // Guardar foto en localStorage para acceso rápido
        if (datos.fotoPerfil) {
            localStorage.setItem('fotoPerfil', datos.fotoPerfil);
        } else {
            localStorage.removeItem('fotoPerfil');
        }
        return datos;
    } catch (e) { return null; }
}

function getAvatarHtml(fotoPerfil, size = 32) {
    if (fotoPerfil) {
        return `<img src="${fotoPerfil}" style="width:${size}px;height:${size}px;border-radius:50%;object-fit:cover">`;
    }
    return `<div style="width:${size}px;height:${size}px;border-radius:50%;background:#ddd;display:flex;align-items:center;justify-content:center;font-size:${size * 0.6}px">👤</div>`;
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

function subirFoto(input) {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async (e) => {
        const base64 = e.target.result;
        await fetch('/users/foto-perfil', {
            method: 'PUT',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ foto: base64 })
        });
        mostrarToast('✓ Foto actualizada');
    };
    reader.readAsDataURL(file);
}