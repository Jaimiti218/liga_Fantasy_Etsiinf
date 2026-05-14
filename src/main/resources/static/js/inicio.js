let usuarioActual = null;

document.addEventListener('DOMContentLoaded', async () => {
    usuarioActual = await obtenerUsuarioActual();

    if (usuarioActual) {
        document.getElementById('saludo-usuario').innerHTML = getAvatarHtml(usuarioActual.fotoPerfil, 28) +
        ' ' + usuarioActual.username;
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