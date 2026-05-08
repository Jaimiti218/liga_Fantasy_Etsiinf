// Inicializar navbar en todas las páginas fantasy
document.addEventListener('DOMContentLoaded', async () => {
    const usuario = await obtenerUsuarioActual();
    if (!usuario) {
        window.location.href = '/fantasy/auth?redirect=fantasy';
        return;
    }
    document.getElementById('nav-username').textContent = usuario.username;
    document.getElementById('menu-username').textContent = '👋 ' + usuario.username;
});

function toggleMenu() {
    const menu = document.getElementById('menu-lateral');
    const overlay = document.getElementById('menu-overlay');
    menu.classList.toggle('hidden');
    overlay.classList.toggle('hidden');
}