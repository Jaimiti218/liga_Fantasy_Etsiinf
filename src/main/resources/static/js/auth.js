// ─── Al cargar la página, decidir qué vista mostrar ───────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const usuario = obtenerSesion();
    if (usuario) {
        document.getElementById('nombre-usuario').textContent = usuario.username;
        mostrarVista('vista-ya-logueado');
    } else {
        mostrarVista('vista-login');
    }
});

// ─── Gestión de vistas ────────────────────────────────────────────────────────
function mostrarVista(idVista) {
    const vistas = ['vista-ya-logueado', 'vista-login', 'vista-registro', 'vista-olvide'];
    vistas.forEach(id => {
        document.getElementById(id).classList.add('hidden');
    });
    document.getElementById(idVista).classList.remove('hidden');
}

function mostrarLogin()          { mostrarVista('vista-login');    limpiarMensajes(); }
function mostrarRegistro()       { mostrarVista('vista-registro'); limpiarMensajes(); }
function mostrarOlvidePassword() { mostrarVista('vista-olvide');   limpiarMensajes(); }

// ─── Sesión en localStorage ───────────────────────────────────────────────────
function guardarSesion(userId, username, role) {
    localStorage.setItem('userId',   userId);
    localStorage.setItem('username', username);
    localStorage.setItem('role',     role ?? 'USER');
}

function obtenerSesion() {
    const userId   = localStorage.getItem('userId');
    const username = localStorage.getItem('username');
    if (userId && username) return { userId, username };
    return null;
}

function cerrarSesion() {
    if (confirm('¿Seguro que quieres cerrar sesión?')) {
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
        mostrarVista('vista-login');
    }
}

// ─── Redirección tras login ───────────────────────────────────────────────────
function obtenerRedirect() {
    const params = new URLSearchParams(window.location.search);
    return params.get('redirect');
}

function entrarTrasLogin() {
    const redirect = obtenerRedirect();
    if (redirect === 'fantasy') {
        window.location.href = '/fantasy/mis-ligas';
    } else {
        window.location.href = '/';
    }
}

// ─── LOGIN ────────────────────────────────────────────────────────────────────
async function hacerLogin() {
    const email    = document.getElementById('email-login').value.trim();
    const password = document.getElementById('password-login').value;

    if (!email || !password) {
        mostrarError('error-login', 'Por favor rellena todos los campos.');
        return;
    }

    try {
        const respuesta = await fetch('/users/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!respuesta.ok) {
            const texto = await respuesta.text();
            mostrarError('error-login', texto || 'Correo o contraseña incorrectos.');
            return;
        }

        const datos = await respuesta.json();
        guardarSesion(datos.id, datos.username, datos.role);
        entrarTrasLogin();

    } catch (e) {
        mostrarError('error-login', 'Error de conexión con el servidor.');
    }
}

// ─── REGISTRO ─────────────────────────────────────────────────────────────────
async function hacerRegistro() {
    const username  = document.getElementById('username-registro').value.trim();
    const email     = document.getElementById('email-registro').value.trim();
    const password  = document.getElementById('password-registro').value;
    const password2 = document.getElementById('password-registro2').value;

    if (!username || !email || !password || !password2) {
        mostrarError('error-registro', 'Por favor rellena todos los campos.');
        return;
    }

    if (password !== password2) {
        mostrarError('error-registro', 'Las contraseñas no coinciden.');
        return;
    }

    if (password.length < 6) {
        mostrarError('error-registro', 'La contraseña debe tener al menos 6 caracteres.');
        return;
    }

    try {
        const respuesta = await fetch('/users/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, username, password })
        });

        if (!respuesta.ok) {
            const texto = await respuesta.text();
            mostrarError('error-registro', texto || 'Error al crear la cuenta.');
            return;
        }

        const datos = await respuesta.json();
        mostrarExito('exito-registro', '¡Cuenta creada! Iniciando sesión...');

        setTimeout(() => {
            guardarSesion(datos.id, datos.username, datos.role);
            entrarTrasLogin();
        }, 1200);

    } catch (e) {
        mostrarError('error-registro', 'Error de conexión con el servidor.');
    }
}

// ─── OLVIDÉ CONTRASEÑA ────────────────────────────────────────────────────────
function enviarRecuperacion() {
    const email = document.getElementById('email-olvide').value.trim();
    if (!email) {
        mostrarError('error-olvide', 'Introduce tu correo electrónico.');
        return;
    }
    mostrarExito('exito-olvide', 'Si existe una cuenta con ese correo, recibirás un email en breve. (Función próximamente disponible)');
}

// ─── Mostrar/ocultar contraseña ───────────────────────────────────────────────
function togglePassword(inputId, boton) {
    const input = document.getElementById(inputId);
    if (input.type === 'password') {
        input.type = 'text';
        boton.textContent = '🙈';
    } else {
        input.type = 'password';
        boton.textContent = '👁';
    }
}

// ─── Utilidades de mensajes ───────────────────────────────────────────────────
function mostrarError(id, texto) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.classList.remove('hidden');
}

function mostrarExito(id, texto) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.classList.remove('hidden');
}

function limpiarMensajes() {
    document.querySelectorAll('.mensaje-error, .mensaje-exito').forEach(el => {
        el.classList.add('hidden');
        el.textContent = '';
    });
}