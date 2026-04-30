document.addEventListener('DOMContentLoaded', () => {
    const userId   = localStorage.getItem('userId');
    const username = localStorage.getItem('username');
    const role     = localStorage.getItem('role');

    if (userId && username) {
        // Hay sesión: mostrar saludo y cambiar botón a "Cerrar sesión"
        document.getElementById('saludo-usuario').textContent = '👋 ' + username;
        document.getElementById('saludo-usuario').classList.remove('hidden');
        document.getElementById('btn-sesion').textContent = 'Cerrar sesión';

        // Si es admin, mostrar botón de gestión
        if (role === 'ADMIN') {
            document.getElementById('btn-gestionar').classList.remove('hidden');
        }
    }
});

function manejarSesion() {
    const userId = localStorage.getItem('userId');
    if (userId) {
        // Hay sesión → cerrar sesión
        if (confirm('¿Seguro que quieres cerrar sesión?')) {
            localStorage.removeItem('userId');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
            window.location.reload();
        }
    } else {
        // No hay sesión → ir al login con redirect a home
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