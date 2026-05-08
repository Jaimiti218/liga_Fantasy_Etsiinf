let todosLosEquipos  = [];
let modoEdicion      = false;
let idEditando       = null;

const POSICIONES = {
    'PORTERO':     '🧤 Portero',
    'DEFENSA':     '🛡 Defensa',
    'MEDIOCENTRO': '⚙️ Mediocentro',
    'DELANTERO':   '⚡ Delantero'
};

document.addEventListener('DOMContentLoaded', async () => {
    const esAdmin = await verificarAdmin();
    if (!esAdmin) return;
    await cargarJugadores();
    await cargarEquipos();
});

async function verificarAdmin() {
    const usuario = await obtenerUsuarioActual();
    if (!usuario || usuario.role !== 'ADMIN') {
        alert('No tienes permisos para acceder a esta página.');
        window.location.href = '/';
        return false;
    }
    return true;
}

async function cargarJugadores() {
    try {
        const res   = await fetch('/jugadores');
        const lista = await res.json();
        renderizarTabla(lista);
    } catch (e) {
        mostrarMensajeGlobal('Error al cargar los jugadores.', 'error');
    }
}

async function cargarEquipos() {
    try {
        const res = await fetch('/equipos');
        todosLosEquipos = await res.json();
    } catch (e) {
        todosLosEquipos = [];
    }
}

function renderizarTabla(jugadores) {
    const tbody = document.getElementById('tbody-jugadores');

    if (jugadores.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="cargando">No hay jugadores todavía.</td></tr>';
        return;
    }

    tbody.innerHTML = jugadores.map(j => `
        <tr>
            <td>${j.fullName}</td>
            <td>${j.nombreEquipo ?? '—'}</td>
            <td>${POSICIONES[j.posicion] ?? j.posicion ?? '—'}</td>
            <td>
                <button class="btn-accion" title="Editar"
                    onclick="abrirModalEditar(${j.id}, '${escapar(j.fullName)}', '${escapar(j.nombreEquipo ?? '')}', '${j.posicion}')">✏️</button>
                <button class="btn-accion" title="Eliminar"
                    onclick="eliminarJugador(${j.id}, '${escapar(j.fullName)}')">🗑️</button>
            </td>
        </tr>
    `).join('');
}

function abrirModalCrear() {
    modoEdicion = false;
    idEditando  = null;
    document.getElementById('modal-titulo').textContent = 'Crear jugador';
    document.getElementById('input-nombre').value       = '';
    document.getElementById('input-equipo').value       = '';
    document.getElementById('input-posicion').value     = 'PORTERO';
    ocultarSugerencias();
    document.getElementById('error-modal').classList.add('hidden');
    document.getElementById('modal-overlay').classList.remove('hidden');
}

function abrirModalEditar(id, nombre, equipo, posicion) {
    modoEdicion = true;
    idEditando  = id;
    document.getElementById('modal-titulo').textContent = 'Editar jugador';
    document.getElementById('input-nombre').value       = nombre;
    document.getElementById('input-equipo').value       = equipo;
    document.getElementById('input-posicion').value     = posicion;
    ocultarSugerencias();
    document.getElementById('error-modal').classList.add('hidden');
    document.getElementById('modal-overlay').classList.remove('hidden');
}

function cerrarModal() {
    document.getElementById('modal-overlay').classList.add('hidden');
    ocultarSugerencias();
}

function autocompletarEquipo(valor) {
    const lista = document.getElementById('sugerencias-equipo');
    if (!valor.trim()) { ocultarSugerencias(); return; }

    const coincidencias = todosLosEquipos.filter(e =>
        e.name.toLowerCase().includes(valor.toLowerCase())
    );

    if (coincidencias.length === 0) { ocultarSugerencias(); return; }

    lista.innerHTML = coincidencias.map(e =>
        `<li onclick="seleccionarEquipo('${escapar(e.name)}')">${e.name}</li>`
    ).join('');
    lista.classList.remove('hidden');
}

function seleccionarEquipo(nombre) {
    document.getElementById('input-equipo').value = nombre;
    ocultarSugerencias();
}

function ocultarSugerencias() {
    document.getElementById('sugerencias-equipo').classList.add('hidden');
}

async function guardarJugador() {
    const nombre   = document.getElementById('input-nombre').value.trim();
    const equipo   = document.getElementById('input-equipo').value.trim();
    const posicion = document.getElementById('input-posicion').value;

    if (!nombre) {
        mostrarErrorModal('El nombre es obligatorio.');
        return;
    }

    try {
        let res;

        if (!modoEdicion) {
            const body = {
                fullName: nombre,
                equipo: equipo ? { name: equipo } : null,
                posicion: posicion
            };
            res = await fetch('/jugadores', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': obtenerCsrfToken()
                },
                credentials: 'include',
                body: JSON.stringify(body)
            });
        } else {
            const body = {
                fullName: nombre,
                nombreEquipo: equipo || null,
                posicion: posicion
            };
            res = await fetch(`/jugadores/${idEditando}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': obtenerCsrfToken()
                },
                credentials: 'include',
                body: JSON.stringify(body)
            });
        }

        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorModal(texto || 'Error al guardar el jugador.');
            return;
        }

        cerrarModal();
        mostrarMensajeGlobal(modoEdicion ? 'Jugador actualizado.' : 'Jugador creado.', 'exito');
        await cargarJugadores();

    } catch (e) {
        mostrarErrorModal('Error de conexión con el servidor.');
    }
}

async function eliminarJugador(id, nombre) {
    if (!confirm(`¿Seguro que quieres eliminar a "${nombre}"?`)) return;

    try {
        const res = await fetch(`/jugadores/${id}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': obtenerCsrfToken() }
        });

        if (!res.ok) {
            mostrarMensajeGlobal('Error al eliminar el jugador.', 'error');
            return;
        }

        mostrarMensajeGlobal('Jugador eliminado.', 'exito');
        await cargarJugadores();

    } catch (e) {
        mostrarMensajeGlobal('Error de conexión con el servidor.', 'error');
    }
}

function mostrarErrorModal(texto) {
    const el = document.getElementById('error-modal');
    el.textContent = texto;
    el.classList.remove('hidden');
}

function mostrarMensajeGlobal(texto, tipo) {
    const el = document.getElementById('mensaje-global');
    el.textContent = texto;
    el.className = 'mensaje-global ' + tipo;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 4000);
}

function escapar(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, '\\"');
}