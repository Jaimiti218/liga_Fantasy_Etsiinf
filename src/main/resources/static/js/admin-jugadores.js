let todosLosEquipos  = [];
let modoEdicion      = false;
let idEditando       = null;

document.addEventListener('DOMContentLoaded', async () => {
    const esAdmin = await verificarAdmin();
    if (!esAdmin) return;
    await cargarJugadores(); // o cargarEquipos()
    await cargarEquipos();   // o cargarJugadores()
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

// ─── Cargar datos ─────────────────────────────────────────────────────────────
async function cargarJugadores() {
    try {
        const res  = await fetch('/jugadores');
        const lista = await res.json();
        renderizarTabla(lista);
    } catch (e) {
        mostrarMensajeGlobal('Error al cargar los jugadores.', 'error');
    }
}

async function cargarEquipos() {
    try {
        const res = await fetch('/equipos');
        todosLosEquipos = await res.json(); // [{id, name, jugadores}, ...]
    } catch (e) {
        todosLosEquipos = [];
    }
}

// ─── Tabla ────────────────────────────────────────────────────────────────────
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
            <td>${j.esPortero ? '✅' : '—'}</td>
            <td>
                <button class="btn-accion" title="Editar" onclick="abrirModalEditar(${j.id}, '${escapar(j.fullName)}', '${escapar(j.nombreEquipo ?? '')}', ${j.esPortero})">✏️</button>
                <button class="btn-accion" title="Eliminar" onclick="eliminarJugador(${j.id}, '${escapar(j.fullName)}')">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// ─── Modal crear ──────────────────────────────────────────────────────────────
function abrirModalCrear() {
    modoEdicion = false;
    idEditando  = null;
    document.getElementById('modal-titulo').textContent  = 'Crear jugador';
    document.getElementById('input-nombre').value        = '';
    document.getElementById('input-equipo').value        = '';
    document.getElementById('input-portero').checked     = false;
    ocultarSugerencias();
    document.getElementById('error-modal').classList.add('hidden');
    document.getElementById('modal-overlay').classList.remove('hidden');
}

// ─── Modal editar ─────────────────────────────────────────────────────────────
function abrirModalEditar(id, nombre, equipo, esPortero) {
    modoEdicion = true;
    idEditando  = id;
    document.getElementById('modal-titulo').textContent  = 'Editar jugador';
    document.getElementById('input-nombre').value        = nombre;
    document.getElementById('input-equipo').value        = equipo;
    document.getElementById('input-portero').checked     = esPortero;
    ocultarSugerencias();
    document.getElementById('error-modal').classList.add('hidden');
    document.getElementById('modal-overlay').classList.remove('hidden');
}

function cerrarModal() {
    document.getElementById('modal-overlay').classList.add('hidden');
    ocultarSugerencias();
}

// ─── Autocompletar equipo ─────────────────────────────────────────────────────
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

// ─── Guardar (crear o editar) ─────────────────────────────────────────────────
async function guardarJugador() {
    const nombre   = document.getElementById('input-nombre').value.trim();
    const equipo   = document.getElementById('input-equipo').value.trim();
    const portero  = document.getElementById('input-portero').checked;

    if (!nombre) {
        mostrarErrorModal('El nombre es obligatorio.');
        return;
    }

    try {
        let res;

        if (!modoEdicion) {
            // CREAR — el backend espera un objeto Jugador con fullName, equipo.name y esPortero
            const body = {
                fullName: nombre,
                equipo: equipo ? { name: equipo } : null,
                esPortero: portero
            };
            res = await fetch('/jugadores', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
        } else {
            // EDITAR — usa JugadorUpdateDTO: fullName, nombreEquipo, esPortero
            const body = {
                fullName: nombre,
                nombreEquipo: equipo || null,
                esPortero: portero
            };
            res = await fetch(`/jugadores/${idEditando}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
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

// ─── Eliminar ─────────────────────────────────────────────────────────────────
async function eliminarJugador(id, nombre) {
    if (!confirm(`¿Seguro que quieres eliminar a "${nombre}"?`)) return;

    try {
        const res = await fetch(`/jugadores/${id}`, { method: 'DELETE' });

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

// ─── Utilidades ───────────────────────────────────────────────────────────────
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