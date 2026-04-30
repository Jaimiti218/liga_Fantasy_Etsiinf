document.addEventListener('DOMContentLoaded', () => {
    const role     = localStorage.getItem('role');
    const username = localStorage.getItem('username');

    // Protección: si no es admin, redirigir
    if (role !== 'ADMIN') {
        alert('No tienes permisos para acceder a esta página.');
        window.location.href = '/';
        return;
    }

    if (username) {
        document.getElementById('saludo-usuario').textContent = '👋 ' + username;
    }
});