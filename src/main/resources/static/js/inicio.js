window.usuarioActual = null;

document.addEventListener('DOMContentLoaded', async () => {
    window.usuarioActual = await obtenerUsuarioActual();

    if (window.usuarioActual) {
        document.getElementById('saludo-usuario').textContent = '👋 ' + window.usuarioActual.username;
        document.getElementById('saludo-usuario').classList.remove('hidden');
        document.getElementById('btn-sesion').textContent = 'Cerrar sesión';

        if (window.usuarioActual.role === 'ADMIN') {
            document.getElementById('btn-gestionar').classList.remove('hidden');
        }
    }
});

function manejarSesion() {
    if (window.usuarioActual) {
        cerrarSesionCompleta();
    } else {
        window.location.href = '/fantasy/auth?redirect=home';
    }
}

function irAlFantasy() {
    if (window.usuarioActual) {
        window.location.href = '/fantasy/mis-ligas';
    } else {
        window.location.href = '/fantasy/auth?redirect=fantasy';
    }
}