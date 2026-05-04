document.addEventListener('DOMContentLoaded', async () => {
    const usuario = await obtenerUsuarioActual();
    if (usuario) {
        document.getElementById('nombre-usuario').textContent = usuario.username;
        mostrarVista('vista-ya-logueado');
    } else {
        mostrarVista('vista-login');
    }
});

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

function entrarAlFantasy() {
    window.location.href = '/fantasy/mis-ligas';
}

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
            credentials: 'include',
            body: JSON.stringify({ email, password })
        });

        if (!respuesta.ok) {
            const texto = await respuesta.text();
            mostrarError('error-login', texto || 'Correo o contraseña incorrectos.');
            return;
        }

        // La cookie JSESSIONID ya se guardó automáticamente
        // Guardamos solo lo necesario para la UI en localStorage
        const datos = await respuesta.json();
        localStorage.setItem('username', datos.username);

        entrarTrasLogin();

    } catch (e) {
        mostrarError('error-login', 'Error de conexión con el servidor.');
    }
}

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
            credentials: 'include',
            body: JSON.stringify({ email, username, password })
        });

        if (!respuesta.ok) {
            const texto = await respuesta.text();
            mostrarError('error-registro', texto || 'Error al crear la cuenta.');
            return;
        }

        mostrarExito('exito-registro', '¡Cuenta creada! Iniciando sesión...');

        setTimeout(() => {
            entrarTrasLogin();
        }, 1200);

    } catch (e) {
        mostrarError('error-registro', 'Error de conexión con el servidor.');
    }
}

function enviarRecuperacion() {
    const email = document.getElementById('email-olvide').value.trim();
    if (!email) {
        mostrarError('error-olvide', 'Introduce tu correo electrónico.');
        return;
    }
    mostrarExito('exito-olvide', 'Si existe una cuenta con ese correo, recibirás un email en breve. (Función próximamente disponible)');
}

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

function cerrarSesion() {
    cerrarSesionCompleta();
}

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