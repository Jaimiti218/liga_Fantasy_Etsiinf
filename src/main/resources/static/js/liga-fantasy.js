let ligaId = null;
let miEquipoId = null;
let clasificacion = [];

document.addEventListener('DOMContentLoaded', async () => {
    // Obtener el ligaId de la URL: /fantasy/liga/3 → 3
    const partes = window.location.pathname.split('/');
    ligaId = parseInt(partes[partes.length - 1]);

    if (!ligaId) {
        window.location.href = '/fantasy/mis-ligas';
        return;
    }

    await cargarLiga();
    await cargarMiEquipo();
    await cargarClasificacion();
});

// ─── Cargar info de la liga ───────────────────────────────────────────────────
async function cargarLiga() {
    try {
        const res = await fetch(`/ligas-fantasy/${ligaId}`, { credentials: 'include' });
        if (!res.ok) { window.location.href = '/fantasy/mis-ligas'; return; }
        const liga = await res.json();
        document.getElementById('nombre-liga-nav').textContent = '⚽ ' + liga.name;
        document.title = liga.name + ' – Fantasy ETSIINF';
    } catch (e) {}
}

// ─── Cargar mi equipo ─────────────────────────────────────────────────────────
async function cargarMiEquipo() {
    try {
        const res = await fetch(`/equipos-fantasy/liga/${ligaId}/mi-equipo`, {
            credentials: 'include'
        });
        if (!res.ok) return;

        const equipo = await res.json();
        miEquipoId = equipo.id;

        document.getElementById('mis-puntos').textContent = equipo.puntos;
        document.getElementById('mi-dinero').textContent = formatearDinero(equipo.dinero);
        document.getElementById('mi-equipo-info').classList.remove('hidden');

    } catch (e) {}
}

// ─── Cargar clasificación ─────────────────────────────────────────────────────
async function cargarClasificacion() {
    try {
        const res = await fetch(`/equipos-fantasy/ligas/${ligaId}/clasificacion`, {
            credentials: 'include'
        });
        if (!res.ok) return;

        clasificacion = await res.json();

        // Actualizar posición en el banner
        if (miEquipoId !== null) {
            const pos = clasificacion.findIndex(e => e.equipoId === miEquipoId);
            if (pos !== -1) {
                document.getElementById('mi-posicion').textContent = (pos + 1) + 'º';
            }
        }

        renderizarClasificacion(clasificacion);

    } catch (e) {
        document.getElementById('tabla-clasificacion').innerHTML =
            '<div class="cargando-ligas">Error al cargar la clasificación.</div>';
    }
}

// ─── Renderizar clasificación ─────────────────────────────────────────────────
function renderizarClasificacion(lista) {
    const contenedor = document.getElementById('tabla-clasificacion');
    if (lista.length === 0) {
        contenedor.innerHTML = '<div class="cargando-ligas">Aún no hay equipos.</div>';
        return;
    }

    contenedor.innerHTML = lista.map((equipo, index) => {
        const pos    = index + 1;
        const esMio  = equipo.equipoId === miEquipoId;
        let clasePos = 'posicion-num';
        if (pos === 1) clasePos += ' top1';
        else if (pos === 2) clasePos += ' top2';
        else if (pos === 3) clasePos += ' top3';

        return `
            <div class="fila-clasificacion ${esMio ? 'mi-equipo-fila' : ''}">
                <span class="${clasePos}">${pos}</span>
                <span class="fila-username ${!esMio ? 'fila-clickable' : ''}"
                      onclick="${!esMio ? `verEquipo(${equipo.equipoId})` : ''}">
                    ${equipo.userNombre}
                </span>
                <span class="fila-puntos">${equipo.puntos} pts</span>
            </div>
        `;
    }).join('');
}

function verEquipo(equipoId) {
    window.location.href = `/fantasy/equipo/${equipoId}?liga=${ligaId}`;
}
// ─── Utilidades ───────────────────────────────────────────────────────────────
function formatearDinero(cantidad) {
    if (cantidad >= 1000000) return (cantidad / 1000000).toFixed(1) + 'M';
    if (cantidad >= 1000) return (cantidad / 1000).toFixed(0) + 'K';
    return cantidad.toString();
}

function irAPlantilla() {
    if (miEquipoId) {
        window.location.href = `/fantasy/plantilla/${miEquipoId}?liga=${ligaId}`;
    }
}

function irAMercado() {
    window.location.href = `/fantasy/mercado/${ligaId}`;
}