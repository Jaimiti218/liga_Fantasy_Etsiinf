let todosLosJugadores   = [];
let jugadoresSeleccionados = [];
let modoEdicion         = false;
let idEditando          = null;

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
async function cargarEquipos() {
    try {
        const res  = await fetch('/equipos');
        const lista = await res.json();
        renderizarTabla(lista);
    } catch (e) {
        mostrarMensajeGlobal('Error al cargar los equipos.', 'error');
    }
}

async function cargarJugadores() {
    try {
        const res = await fetch('/jugadores');
        todosLosJugadores = await res.json();
    } catch (e) {
        todosLosJugadores = [];
    }
}

// ─── Tabla ────────────────────────────────────────────────────────────────────
function renderizarTabla(equipos) {
    const tbody = document.getElementById('tbody-equipos');

    if (equipos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="cargando">No hay equipos todavía.</td></tr>';
        return;
    }

    tbody.innerHTML = equipos.map(e => {
        const jugadoresB64 = btoa(unescape(encodeURIComponent(JSON.stringify(e.jugadores))));
        return `
        <tr>
            <td>${e.name}</td>
            <td>${e.jugadores.length > 0 ? e.jugadores.join(', ') : '—'}</td>
            <td>
                <button class="btn-accion" title="Editar"
                    data-id="${e.id}"
                    data-nombre="${escapar(e.name)}"
                    data-jugadores="${jugadoresB64}"
                    onclick="abrirModalEditarDesdeBoton(this)">✏️</button>
                <button class="btn-accion" title="Eliminar"
                    onclick="eliminarEquipo(${e.id}, '${escapar(e.name)}')">🗑️</button>
            </td>
        </tr>`;
    }).join('');
}


function abrirModalEditarDesdeBoton(boton) {
    const id        = parseInt(boton.dataset.id);
    const nombre    = boton.dataset.nombre;
    const jugadores = JSON.parse(decodeURIComponent(escape(atob(boton.dataset.jugadores))));
    abrirModalEditar(id, nombre, jugadores);
}

// ─── Modal crear ──────────────────────────────────────────────────────────────
function abrirModalCrear() {
    modoEdicion            = false;
    idEditando             = null;
    jugadoresSeleccionados = [];
    document.getElementById('modal-titulo').textContent     = 'Crear equipo';
    document.getElementById('input-nombre').value           = '';
    document.getElementById('input-jugador-buscar').value   = '';
    document.getElementById('jugadores-seleccionados').innerHTML = '';
    document.getElementById('sugerencias-jugador').classList.add('hidden');
    document.getElementById('error-modal').classList.add('hidden');
    document.getElementById('modal-overlay').classList.remove('hidden');
}

// ─── Modal editar ─────────────────────────────────────────────────────────────
function abrirModalEditar(id, nombre, jugadores) {
    modoEdicion            = true;
    idEditando             = id;
    jugadoresSeleccionados = [...jugadores];
    document.getElementById('modal-titulo').textContent     = 'Editar equipo';
    document.getElementById('input-nombre').value           = nombre;
    document.getElementById('input-jugador-buscar').value   = '';
    document.getElementById('sugerencias-jugador').classList.add('hidden');
    document.getElementById('error-modal').classList.add('hidden');
    renderizarTags();
    document.getElementById('modal-overlay').classList.remove('hidden');
}

function cerrarModal() {
    document.getElementById('modal-overlay').classList.add('hidden');
}

// ─── Autocompletar jugadores ──────────────────────────────────────────────────
function autocompletarJugador(valor) {
    const lista = document.getElementById('sugerencias-jugador');
    if (!valor.trim()) { lista.classList.add('hidden'); return; }

    const coincidencias = todosLosJugadores.filter(j =>
        j.fullName.toLowerCase().includes(valor.toLowerCase()) &&
        !jugadoresSeleccionados.includes(j.fullName)
    );

    if (coincidencias.length === 0) { lista.classList.add('hidden'); return; }

    lista.innerHTML = coincidencias.map(j =>
        `<li onclick="seleccionarJugador('${escapar(j.fullName)}')">${j.fullName}</li>`
    ).join('');
    lista.classList.remove('hidden');
}

function seleccionarJugador(nombre) {
    if (!jugadoresSeleccionados.includes(nombre)) {
        jugadoresSeleccionados.push(nombre);
        renderizarTags();
    }
    document.getElementById('input-jugador-buscar').value = '';
    document.getElementById('sugerencias-jugador').classList.add('hidden');
}

function quitarJugador(nombre) {
    jugadoresSeleccionados = jugadoresSeleccionados.filter(j => j !== nombre);
    renderizarTags();
}

function renderizarTags() {
    const contenedor = document.getElementById('jugadores-seleccionados');
    contenedor.innerHTML = jugadoresSeleccionados.map(nombre => `
        <span class="tag">
            ${nombre}
            <button onclick="quitarJugador('${escapar(nombre)}')">✕</button>
        </span>
    `).join('');
}

// ─── Guardar ─────────────────────────────────────────────────────────────────
async function guardarEquipo() {
    const nombre = document.getElementById('input-nombre').value.trim();
    const inputBuscar = document.getElementById('input-jugador-buscar').value.trim();

    if (!nombre) {
        mostrarErrorModal('El nombre del equipo es obligatorio.');
        return;
    }

    // Si hay texto en el buscador sin confirmar, añadirlo a la lista
    if (inputBuscar && !jugadoresSeleccionados.includes(inputBuscar)) {
        jugadoresSeleccionados.push(inputBuscar);
        document.getElementById('input-jugador-buscar').value = '';
        document.getElementById('sugerencias-jugador').classList.add('hidden');
        renderizarTags();
    }

    try {
        let res;

        if (!modoEdicion) {
            const body = {
                name: nombre,
                jugadores: jugadoresSeleccionados.map(n => ({ fullName: n }))
            };
            res = await fetch('/equipos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
        } else {
            const body = {
                nombre: nombre,
                jugadores: jugadoresSeleccionados
            };
            res = await fetch(`/equipos/${idEditando}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
        }

        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorModal(texto || 'Error al guardar el equipo.');
            return;
        }

        cerrarModal();
        mostrarMensajeGlobal(modoEdicion ? 'Equipo actualizado.' : 'Equipo creado.', 'exito');
        await cargarEquipos();

    } catch (e) {
        mostrarErrorModal('Error de conexión con el servidor.');
    }
}

// ─── Eliminar ─────────────────────────────────────────────────────────────────
async function eliminarEquipo(id, nombre) {
    if (!confirm(`¿Seguro que quieres eliminar el equipo "${nombre}"?`)) return;

    try {
        const res = await fetch(`/equipos/${id}`, { method: 'DELETE' });

        if (!res.ok) {
            mostrarMensajeGlobal('Error al eliminar el equipo.', 'error');
            return;
        }

        mostrarMensajeGlobal('Equipo eliminado.', 'exito');
        await cargarEquipos();

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
    return String(str).replace(/'/g, "\\'").replace(/"/g, '\\"');
}