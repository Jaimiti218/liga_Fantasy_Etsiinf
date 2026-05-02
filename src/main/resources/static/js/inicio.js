document.addEventListener('DOMContentLoaded', async () => {
    const usuario = await obtenerUsuarioActual();

    if (usuario) {
        document.getElementById('saludo-usuario').textContent = '👋 ' + usuario.username;
        document.getElementById('saludo-usuario').classList.remove('hidden');
        document.getElementById('btn-sesion').textContent = 'Cerrar sesión';

        if (usuario.role === 'ADMIN') {
            document.getElementById('btn-gestionar').classList.remove('hidden');
        }
    }
});

function manejarSesion() {
    const userId = localStorage.getItem('userId');
    if (userId) {
        cerrarSesionCompleta();
    } else {
        window.location.href = '/fantasy/auth?redirect=home';
    }
}

function irAlFantasy() {
    const userId = localStorage.getItem('userId');
    if (userId) {
        window.location.href = '/fantasy/mis-ligas';
    } else {
        window.location.href = '/fantasy/auth?redirect=fantasy';
    }
}