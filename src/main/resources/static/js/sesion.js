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

function mostrarCargando(texto = 'Cargando...') {
    let overlay = document.getElementById('loading-overlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'loading-overlay';
        document.body.appendChild(overlay);
    }

    overlay.style.cssText = `
        position: fixed;
        top: 0; left: 0;
        width: 100%; height: 100%;
        background: rgba(0,0,0,0.45);
        z-index: 99999;
        display: flex;
        align-items: flex-start;
        justify-content: center;
        padding-top: 70px;
    `;

    overlay.innerHTML = `
        <div style="
            background: white;
            border-radius: 12px;
            padding: 0.85rem 1.5rem;
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 0.75rem;
            box-shadow: 0 4px 24px rgba(0,0,0,0.25);
            border: 1px solid #e0e0e0;
            animation: slideDown 0.2s ease;
        ">
            <div style="
                width: 22px;
                height: 22px;
                border: 3px solid rgba(255,107,0,0.2);
                border-top-color: #ff6b00;
                border-radius: 50%;
                animation: spin 0.75s linear infinite;
                flex-shrink: 0;
            "></div>
            <span style="
                color: #1a1a2e;
                font-size: 0.9rem;
                font-weight: 600;
                white-space: nowrap;
            ">${texto}</span>
        </div>
        <style>
            @keyframes spin { to { transform: rotate(360deg); } }
            @keyframes slideDown {
                from { transform: translateY(-15px); opacity: 0; }
                to   { transform: translateY(0); opacity: 1; }
            }
        </style>
    `;
}

function ocultarCargando() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) overlay.style.display = 'none';
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