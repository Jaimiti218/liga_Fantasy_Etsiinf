document.addEventListener('DOMContentLoaded', async () => {
    const usuario = await obtenerUsuarioActual();

    if (!usuario || usuario.role !== 'ADMIN') {
        alert('No tienes permisos para acceder a esta página.');
        window.location.href = '/';
        return;
    }

    document.getElementById('saludo-usuario').textContent = '👋 ' + usuario.username;
});