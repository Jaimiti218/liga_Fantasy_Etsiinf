let usuarioActual = null;

document.addEventListener('DOMContentLoaded', async () => {
    usuarioActual = await obtenerUsuarioActual();

    if (usuarioActual) {
        document.getElementById('saludo-usuario').style.display = 'flex';
        document.getElementById('saludo-usuario').style.alignItems = 'center';
        document.getElementById('saludo-usuario').style.gap = '0.4rem';
        document.getElementById('saludo-usuario').innerHTML =
            (usuarioActual.fotoPerfil
                ? `<img src="${usuarioActual.fotoPerfil}" style="width:28px;height:28px;border-radius:50%;object-fit:cover">`
                : `<span style="width:28px;height:28px;border-radius:50%;background:rgba(255,255,255,0.3);display:flex;align-items:center;justify-content:center">👤</span>`)
            + `<span> ${usuarioActual.username}</span>`;
        document.getElementById('saludo-usuario').classList.remove('hidden');
        document.getElementById('btn-sesion').textContent = 'Cerrar sesión';

        if (usuarioActual.role === 'ADMIN') {
            document.getElementById('btn-gestionar').classList.remove('hidden');
        }
    }
});

function manejarSesion() {
    if (usuarioActual) {
        cerrarSesionCompleta();
    } else {
        location.href = '/fantasy/auth?redirect=home';
    }
}

function irAlFantasy() {
    if (usuarioActual) {
        location.href = '/fantasy/mis-ligas';
    } else {
        location.href = '/fantasy/auth?redirect=fantasy';
    }
}

function abrirMenuPerfil() {
    const foto = usuarioActual?.fotoPerfil;
    const avatarEl = document.getElementById('avatar-perfil-grande');
    avatarEl.innerHTML = foto
        ? `<img src="${foto}" style="width:100%;height:100%;object-fit:cover">`
        : '👤';
    document.getElementById('perfil-nombre-display').textContent = usuarioActual?.username || '';
    document.getElementById('input-nuevo-username').value = usuarioActual?.username || '';
    document.getElementById('error-perfil').classList.add('hidden');
    document.getElementById('exito-perfil').classList.add('hidden');
    document.getElementById('modal-perfil').classList.remove('hidden');
}

function cambiarFoto(input) {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async (e) => {
        const base64 = e.target.result;
        try {
            const res = await fetch('/users/foto-perfil', {
                method: 'PUT',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ foto: base64 })
            });
            if (!res.ok) {
                document.getElementById('error-perfil').textContent = 'Error al guardar la foto.';
                document.getElementById('error-perfil').classList.remove('hidden');
                return;
            }
            // Actualizar estado local
            usuarioActual.fotoPerfil = base64;
            // Actualizar vista previa en el modal
            document.getElementById('avatar-perfil-grande').innerHTML =
                `<img src="${base64}" style="width:100%;height:100%;object-fit:cover">`;
            // Actualizar avatar en la navbar
            actualizarNavbarAvatar();
            // Mostrar mensaje de éxito
            document.getElementById('exito-perfil').textContent = '✓ Foto actualizada';
            document.getElementById('exito-perfil').classList.remove('hidden');
            document.getElementById('error-perfil').classList.add('hidden');
        } catch (e) {
            document.getElementById('error-perfil').textContent = 'Error de conexión.';
            document.getElementById('error-perfil').classList.remove('hidden');
        }
    };
    reader.readAsDataURL(file);
}

function cerrarMenuPerfil() {
    document.getElementById('modal-perfil').classList.add('hidden');
}

function cambiarFoto(input) {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async (e) => {
        const base64 = e.target.result;
        const res = await fetch('/users/foto-perfil', {
            method: 'PUT',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ foto: base64 })
        });
        if (!res.ok) return;
        // Actualizar vista previa
        document.getElementById('avatar-perfil-grande').innerHTML =
            `<img src="${base64}" style="width:100%;height:100%;object-fit:cover">`;
        usuarioActual.fotoPerfil = base64;
        // Actualizar navbar
        document.getElementById('saludo-usuario').querySelector('img')?.remove();
    };
    reader.readAsDataURL(file);
}

async function guardarPerfil() {
    const nuevoUsername = document.getElementById('input-nuevo-username').value.trim();
    if (!nuevoUsername) {
        document.getElementById('error-perfil').textContent = 'El nombre no puede estar vacío.';
        document.getElementById('error-perfil').classList.remove('hidden');
        return;
    }

    try {
        const res = await fetch('/users/username', {
            method: 'PUT',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: nuevoUsername })
        });
        if (!res.ok) {
            const texto = await res.text();
            document.getElementById('error-perfil').textContent = texto || 'Error al guardar.';
            document.getElementById('error-perfil').classList.remove('hidden');
            return;
        }
        usuarioActual.username = nuevoUsername;
        document.getElementById('perfil-nombre-display').textContent = nuevoUsername;
        actualizarNavbarAvatar();
        document.getElementById('exito-perfil').textContent = '✓ Nombre actualizado';
        document.getElementById('exito-perfil').classList.remove('hidden');
        document.getElementById('error-perfil').classList.add('hidden');
    } catch (e) {
        document.getElementById('error-perfil').textContent = 'Error de conexión.';
        document.getElementById('error-perfil').classList.remove('hidden');
    }
}

function actualizarNavbarAvatar() {
    const saludo = document.getElementById('saludo-usuario');
    saludo.innerHTML =
        (usuarioActual.fotoPerfil
            ? `<img src="${usuarioActual.fotoPerfil}" style="width:28px;height:28px;border-radius:50%;object-fit:cover">`
            : `<span style="width:28px;height:28px;border-radius:50%;background:rgba(255,255,255,0.3);display:flex;align-items:center;justify-content:center">👤</span>`)
        + `<span> ${usuarioActual.username}</span>`;
}