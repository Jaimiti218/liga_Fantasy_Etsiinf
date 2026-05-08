let misLigas = [];
let dropdownAbierto = null;

document.addEventListener('DOMContentLoaded', async () => {
    await cargarMisLigas();

    // Cerrar dropdown al hacer click fuera
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.liga-card-menu')) {
            cerrarDropdowns();
        }
    });
});

// ─── Cargar ligas del usuario ─────────────────────────────────────────────────
async function cargarMisLigas() {
    try {
        const res = await fetch('/ligas-fantasy/usuario/mis-ligas', {
            credentials: 'include'
        });

        if (!res.ok) {
            mostrarMensajeGlobal('Error al cargar las ligas.', 'error');
            return;
        }

        const ligas = await res.json();
        misLigas = ligas;

        if (ligas.length === 0) {
            renderizarSinLigas();
        } else {
            await renderizarLigas(ligas);
        }

    } catch (e) {
        mostrarMensajeGlobal('Error de conexión.', 'error');
    }
}

// ─── Renderizar ligas ─────────────────────────────────────────────────────────
async function renderizarLigas(ligas) {
    const contenedor = document.getElementById('lista-ligas');
    contenedor.innerHTML = '';

    for (const liga of ligas) {
        // Obtener info del equipo del usuario en esta liga
        let miEquipo = null;
        let posicion = '—';

        try {
            const resEquipo = await fetch(`/equipos-fantasy/liga/${liga.id}/mi-equipo`, {
                credentials: 'include'
            });
            if (resEquipo.ok) {
                miEquipo = await resEquipo.json();
            }

            // Obtener posición en la clasificación
            const resClasif = await fetch(`/equipos-fantasy/ligas/${liga.id}/clasificacion`, {
                credentials: 'include'
            });
            if (resClasif.ok && miEquipo) {
                const clasificacion = await resClasif.json();
                const pos = clasificacion.findIndex(e => e.equipoId === miEquipo.id);
                posicion = pos !== -1 ? (pos + 1) + 'º' : '—';
            }
        } catch (e) {}

        const card = document.createElement('div');
        card.className = 'liga-card';
        card.innerHTML = `
            <div class="liga-card-info" onclick="entrarALiga(${liga.id})">
                <div class="liga-card-nombre">${liga.name}</div>
                <div class="liga-card-stats">
                    <div class="liga-stat">
                        <span class="liga-stat-label">Posición</span>
                        <span class="liga-stat-valor">${posicion}</span>
                    </div>
                    <div class="liga-stat">
                        <span class="liga-stat-label">Puntos</span>
                        <span class="liga-stat-valor">${miEquipo ? miEquipo.puntos : '—'}</span>
                    </div>
                    <div class="liga-stat">
                        <span class="liga-stat-label">Dinero</span>
                        <span class="liga-stat-valor">${miEquipo ? formatearDinero(miEquipo.dinero) : '—'}</span>
                    </div>
                </div>
            </div>
            <div class="liga-card-menu">
                <button class="btn-tres-puntos" onclick="toggleDropdown(event, ${liga.id}, '${escapar(liga.code)}')">⋯</button>
                <div id="dropdown-${liga.id}" class="dropdown-menu hidden">
                    <div class="dropdown-item" onclick="mostrarModalInvitar('${escapar(liga.code)}')">
                        🔗 Invitar amigo
                    </div>
                    <div class="dropdown-item danger" onclick="confirmarAbandonar(${liga.id}, '${escapar(liga.name)}')">
                        🚪 Abandonar liga
                    </div>
                </div>
            </div>
        `;
        contenedor.appendChild(card);
    }
}

function renderizarSinLigas() {
    document.getElementById('lista-ligas').innerHTML = `
        <div class="no-ligas">
            <h3>Aún no estás en ninguna liga</h3>
            <p>Crea una nueva liga o únete a una con el código de un amigo.</p>
        </div>
    `;
}

// ─── Entrar a una liga ────────────────────────────────────────────────────────
function entrarALiga(ligaId) {
    window.location.href = `/fantasy/liga/${ligaId}`;
}

// ─── Dropdown ─────────────────────────────────────────────────────────────────
function toggleDropdown(event, ligaId, code) {
    event.stopPropagation();
    const dropdown = document.getElementById(`dropdown-${ligaId}`);
    const estaAbierto = !dropdown.classList.contains('hidden');
    cerrarDropdowns();
    if (!estaAbierto) {
        dropdown.classList.remove('hidden');
        dropdownAbierto = ligaId;
    }
}

function cerrarDropdowns() {
    document.querySelectorAll('.dropdown-menu').forEach(d => d.classList.add('hidden'));
    dropdownAbierto = null;
}

// ─── Modal crear liga ─────────────────────────────────────────────────────────
function abrirModalCrear() {
    document.getElementById('modal-crear').classList.remove('hidden');
    document.getElementById('input-nombre-liga').value = '';
    document.getElementById('error-crear').classList.add('hidden');
}

async function crearLiga() {
    const nombre = document.getElementById('input-nombre-liga').value.trim();
    if (!nombre) {
        mostrarErrorModal('error-crear', 'El nombre es obligatorio.');
        return;
    }

    try {
        const res = await fetch(`/ligas-fantasy/crear?nombre=${encodeURIComponent(nombre)}`, {
            method: 'POST',
            credentials: 'include'
        });

        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorModal('error-crear', texto || 'Error al crear la liga.');
            return;
        }

        cerrarModales();
        mostrarMensajeGlobal('¡Liga creada!', 'exito');
        await cargarMisLigas();

    } catch (e) {
        mostrarErrorModal('error-crear', 'Error de conexión.');
    }
}

// ─── Modal unirse a liga ──────────────────────────────────────────────────────
function abrirModalUnirse() {
    document.getElementById('modal-unirse').classList.remove('hidden');
    document.getElementById('input-codigo-liga').value = '';
    document.getElementById('error-unirse').classList.add('hidden');
}

async function unirseALiga() {
    const codigo = document.getElementById('input-codigo-liga').value.trim();
    if (!codigo) {
        mostrarErrorModal('error-unirse', 'Introduce el código de la liga.');
        return;
    }

    try {
        const res = await fetch(`/ligas-fantasy/unirse?code=${encodeURIComponent(codigo)}`, {
            method: 'POST',
            credentials: 'include'
        });

        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorModal('error-unirse', texto || 'Error al unirse a la liga.');
            return;
        }

        cerrarModales();
        mostrarMensajeGlobal('¡Te has unido a la liga!', 'exito');
        await cargarMisLigas();

    } catch (e) {
        mostrarErrorModal('error-unirse', 'Error de conexión.');
    }
}

// ─── Modal invitar ────────────────────────────────────────────────────────────
function mostrarModalInvitar(codigo) {
    cerrarDropdowns();
    document.getElementById('codigo-mostrar').textContent = codigo;
    document.getElementById('exito-copiar').classList.add('hidden');
    document.getElementById('modal-invitar').classList.remove('hidden');
}

function copiarCodigo() {
    const codigo = document.getElementById('codigo-mostrar').textContent;
    navigator.clipboard.writeText(codigo).then(() => {
        document.getElementById('exito-copiar').classList.remove('hidden');
    });
}

// ─── Abandonar liga ───────────────────────────────────────────────────────────
async function confirmarAbandonar(ligaId, nombre) {
    cerrarDropdowns();
    if (!confirm(`¿Seguro que quieres abandonar la liga "${nombre}"? Perderás tu equipo y puntos.`)) return;

    try {
        const res = await fetch(`/ligas-fantasy/${ligaId}/abandonar`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (!res.ok) {
            mostrarMensajeGlobal('Error al abandonar la liga.', 'error');
            return;
        }

        mostrarMensajeGlobal('Has abandonado la liga.', 'exito');
        await cargarMisLigas();

    } catch (e) {
        mostrarMensajeGlobal('Error de conexión.', 'error');
    }
}

// ─── Cerrar modales ───────────────────────────────────────────────────────────
function cerrarModales() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.add('hidden'));
}

// ─── Utilidades ───────────────────────────────────────────────────────────────
function formatearDinero(cantidad) {
    if (cantidad >= 1000000) return (cantidad / 1000000).toFixed(1) + 'M';
    if (cantidad >= 1000) return (cantidad / 1000).toFixed(0) + 'K';
    return cantidad.toString();
}

function mostrarErrorModal(id, texto) {
    const el = document.getElementById(id);
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