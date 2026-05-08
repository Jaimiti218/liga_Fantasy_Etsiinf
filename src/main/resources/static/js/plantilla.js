// ─── Estado global ──────────────────────────────────────────────────────────
let equipoId             = null;
let ligaId               = null;
let plantillaData        = [];
let alineacionActual     = [];   // array de 7 posiciones, cada una es un ID o null
let formacionActual      = '2-2-2';
let posicionSeleccionada = null;
let candidatoSeleccionado = null;
let jornadaActual        = 1;
let accionesAbierto      = null;
let jugadorClausulaActual = null;
const MAX_JUGADORES      = 13;

// Mapeo posición → emoji e icono
const ICONOS_POSICION = {
    'PORTERO':     '🧤',
    'DEFENSA':     '🛡',
    'MEDIOCENTRO': '⚙️',
    'DELANTERO':   '⚡'
};

// Qué posición juega en cada línea de la formación
// Formación "D-M-DEL" → linea 1: DEFENSA, linea 2: MEDIOCENTRO, linea 3: DELANTERO
const POSICION_POR_LINEA = {
    0: 'PORTERO',
    1: 'DEFENSA',
    2: 'MEDIOCENTRO',
    3: 'DELANTERO'
};

// ─── Init ────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
    const partes = window.location.pathname.split('/');
    equipoId = parseInt(partes[partes.length - 1]);
    ligaId   = parseInt(new URLSearchParams(window.location.search).get('liga'));

    if (!equipoId) { window.location.href = '/fantasy/mis-ligas'; return; }

    await cargarPlantilla();
    cambiarTab('alineacion');

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.btn-acciones')) cerrarAcciones();
    });
});

function volverALiga() {
    window.location.href = `/fantasy/liga/${ligaId}`;
}

// ─── Cargar plantilla ────────────────────────────────────────────────────────
async function cargarPlantilla() {
    try {
        const res = await fetch(`/equipos-fantasy/${equipoId}/plantilla`, {
            credentials: 'include'
        });
        if (!res.ok) { plantillaData = []; return; }
        plantillaData = await res.json();

        // Reconstruir alineación respetando el orden por posición
        // Los alineados los ordenamos: primero portero, luego defensas, medios, delanteros
        const alineados = plantillaData.filter(j => j.alineado);
        const porteroAlin    = alineados.filter(j => j.posicion === 'PORTERO');
        const defensasAlin   = alineados.filter(j => j.posicion === 'DEFENSA');
        const mediosAlin     = alineados.filter(j => j.posicion === 'MEDIOCENTRO');
        const delantAlin     = alineados.filter(j => j.posicion === 'DELANTERO');

        const lineas = parsearFormacion(formacionActual);
        // Reconstruir array de 7 posiciones [portero, ...defensas, ...medios, ...delanteros]
        const nuevaAlin = [
            porteroAlin[0]?.id ?? null,
            ...rellenarLinea(defensasAlin, lineas[0]),
            ...rellenarLinea(mediosAlin, lineas[1]),
            ...rellenarLinea(delantAlin, lineas[2])
        ];
        alineacionActual = nuevaAlin;

        const resEquipo = await fetch(`/equipos-fantasy/${equipoId}`, {
            credentials: 'include'
        });
        if (resEquipo.ok) {
            const equipo = await resEquipo.json();
            formacionActual = equipo.formacion || '2-2-2';
        }

        actualizarResumen();
    } catch (e) {
        plantillaData = [];
    }
}

function rellenarLinea(jugadores, cantidad) {
    const arr = jugadores.slice(0, cantidad).map(j => j.id);
    while (arr.length < cantidad) arr.push(null);
    return arr;
}

// ─── Tabs ────────────────────────────────────────────────────────────────────
function cambiarTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-seccion').forEach(s => s.classList.add('hidden'));

    document.getElementById(`tab-${tab}`).classList.add('active');
    document.getElementById(`seccion-${tab}`).classList.remove('hidden');

    if (tab === 'alineacion') renderizarCampo();
    if (tab === 'plantilla')  renderizarPlantilla();
    if (tab === 'puntos')     renderizarPuntos();
}

// ─── TAB ALINEACIÓN ──────────────────────────────────────────────────────────
function renderizarCampo() {
    const lineas = parsearFormacion(formacionActual);
    document.getElementById('select-formacion').value = formacionActual;

    // Portero (posición 0)
    renderizarLinea('linea-portero', [alineacionActual[0] ?? null], 0, 'PORTERO');

    // Líneas de campo según formación
    let offset = 1;
    const posicionesPorLinea = ['DEFENSA', 'MEDIOCENTRO', 'DELANTERO'];
    lineas.forEach((cantidad, idx) => {
        const idsLinea = alineacionActual.slice(offset, offset + cantidad);
        while (idsLinea.length < cantidad) idsLinea.push(null);
        renderizarLinea(`linea-${idx + 1}`, idsLinea, offset, posicionesPorLinea[idx]);
        offset += cantidad;
    });
}

function renderizarLinea(lineaId, ids, offsetInicial, posicionLinea) {
    const linea = document.getElementById(lineaId);
    linea.innerHTML = '';

    ids.forEach((jfId, i) => {
        const posGlobal = offsetInicial + i;
        const jugador   = jfId ? plantillaData.find(j => j.id === jfId) : null;

        const carta = document.createElement('div');
        carta.className = 'carta-jugador' + (jfId ? '' : ' vacio');
        carta.onclick   = () => abrirSeleccion(posGlobal, posicionLinea);

        if (jugador) {
            carta.innerHTML = `
                <div class="carta-foto">
                    <span>${ICONOS_POSICION[jugador.posicion] ?? '👤'}</span>
                    <div class="escudo-mini">⚽</div>
                </div>
                <div class="carta-nombre">${abreviarNombre(jugador.nombre)}</div>
            `;
        } else {
            carta.innerHTML = `
                <div class="carta-foto" style="background:rgba(255,255,255,0.2)">
                    <span style="color:rgba(255,255,255,0.6);font-size:1.1rem">
                        ${ICONOS_POSICION[posicionLinea] ?? '+'}
                    </span>
                </div>
                <div class="carta-nombre" style="color:rgba(255,255,255,0.7);font-size:0.58rem">
                    ${posicionLinea.charAt(0) + posicionLinea.slice(1).toLowerCase()}
                </div>
            `;
        }
        linea.appendChild(carta);
    });
}

// ─── Selección de jugador ────────────────────────────────────────────────────
function abrirSeleccion(posicion, posicionLinea) {
    posicionSeleccionada  = posicion;
    candidatoSeleccionado = null;
    document.getElementById('btn-confirmar').disabled = true;

    const idActual = alineacionActual[posicion] ?? null;

    // Candidatos: jugadores de la posición correcta que no estén ya alineados
    // excepto el que ya está en esta posición (que no aparece para no confundir)
    const disponibles = plantillaData.filter(j => {
        if (j.posicion !== posicionLinea) return false;
        if (alineacionActual.includes(j.id) && j.id !== idActual) return false;
        if (j.id === idActual) return false; // el que ya está no se muestra como opción
        return true;
    });

    const lista = document.getElementById('lista-candidatos');
    let html = '';

    // Opción de quitar al jugador actual
    if (idActual !== null) {
        html += `<div class="candidato-item candidato-quitar" onclick="quitarJugadorPosicion()">
            ✕ Quitar jugador
        </div>`;
    }

    if (disponibles.length === 0) {
        html += '<div style="color:#999;font-size:0.85rem;padding:0.5rem">No hay más jugadores disponibles para esta posición</div>';
    } else {
        html += disponibles.map(j => `
            <div class="candidato-item" id="candidato-${j.id}" onclick="seleccionarCandidato(${j.id})">
                ${ICONOS_POSICION[j.posicion] ?? '👤'} ${j.nombre}
            </div>
        `).join('');
    }

    lista.innerHTML = html;
    document.getElementById('panel-seleccion').classList.remove('hidden');
}

function quitarJugadorPosicion() {
    if (posicionSeleccionada === null) return;
    const nuevaAlineacion = [...alineacionActual];
    nuevaAlineacion[posicionSeleccionada] = null;
    alineacionActual = nuevaAlineacion;
    cerrarPanelSeleccion();
    renderizarCampo();
}

function seleccionarCandidato(jfId) {
    document.querySelectorAll('.candidato-item').forEach(el => el.classList.remove('seleccionado'));
    const el = document.getElementById(`candidato-${jfId}`);
    if (el) el.classList.add('seleccionado');
    candidatoSeleccionado = jfId;
    document.getElementById('btn-confirmar').disabled = false;
}

function confirmarCambio() {
    if (candidatoSeleccionado === null || posicionSeleccionada === null) return;

    const nuevaAlineacion = [...alineacionActual];
    while (nuevaAlineacion.length < 7) nuevaAlineacion.push(null);
    nuevaAlineacion[posicionSeleccionada] = candidatoSeleccionado;

    alineacionActual = nuevaAlineacion;
    cerrarPanelSeleccion();
    renderizarCampo();
}

function cerrarPanelSeleccion() {
    document.getElementById('panel-seleccion').classList.add('hidden');
    posicionSeleccionada  = null;
    candidatoSeleccionado = null;
}

// ─── Cambiar formación ────────────────────────────────────────────────────────
function cambiarFormacion(nuevaFormacion) {
    formacionActual = nuevaFormacion;
    // Al cambiar formación, resetear solo las posiciones de campo (no el portero)
    // pero mantener los jugadores que sigan siendo válidos para sus nuevas líneas
    const portero = alineacionActual[0] ?? null;
    const lineas  = parsearFormacion(nuevaFormacion);

    // Reconstruir respetando posiciones
    const defensasActuales   = obtenerAlineadosDePosicion('DEFENSA');
    const mediosActuales     = obtenerAlineadosDePosicion('MEDIOCENTRO');
    const delantActuales     = obtenerAlineadosDePosicion('DELANTERO');

    alineacionActual = [
        portero,
        ...rellenarLinea(defensasActuales.map(id => ({ id })), lineas[0]),
        ...rellenarLinea(mediosActuales.map(id => ({ id })), lineas[1]),
        ...rellenarLinea(delantActuales.map(id => ({ id })), lineas[2])
    ];

    renderizarCampo();
    actualizarResumen();
}

function obtenerAlineadosDePosicion(posicion) {
    return alineacionActual
        .filter(id => id !== null)
        .filter(id => {
            const j = plantillaData.find(p => p.id === id);
            return j && j.posicion === posicion;
        });
}

// ─── Guardar alineación ───────────────────────────────────────────────────────
async function guardarAlineacion() {
    const ids = alineacionActual.filter(id => id !== null);

    if (ids.length < 7) {
        // Avisar pero guardar igualmente
        const confirmar = confirm(
            `Solo tienes ${ids.length} jugadores alineados de 7. No puntuarás en la siguiente jornada con los huecos vacíos. ¿Guardar de todos modos?`
        );
        if (!confirmar) return;
    }

    try {
        const res = await fetch(`/equipos-fantasy/${equipoId}/alineacion`, {
            method: 'PUT',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                jugadorFantasyIds: ids,
                formacion: formacionActual   // ← siempre mandamos la formación actual
            })
        });

        if (!res.ok) {
            const texto = await res.text();
            alert(texto || 'Error al guardar la alineación.');
            return;
        }

        mostrarToast('✓ Alineación guardada');
        await cargarPlantilla();
        renderizarCampo();
    } catch (e) {
        alert('Error de conexión.');
    }
}

// ─── Resumen ──────────────────────────────────────────────────────────────────
function actualizarResumen() {
    const valorTotal = plantillaData.reduce((sum, j) => sum + j.valorMercado, 0);
    document.getElementById('valor-equipo').textContent    = formatearDinero(valorTotal);
    document.getElementById('num-jugadores').textContent   = `${plantillaData.length} / ${MAX_JUGADORES}`;
    document.getElementById('formacion-actual').textContent = formacionActual;
}

// ─── TAB PLANTILLA ────────────────────────────────────────────────────────────
function renderizarPlantilla() {
    const contenedor = document.getElementById('lista-plantilla');

    if (plantillaData.length === 0) {
        contenedor.innerHTML = '<div class="cargando-ligas">No tienes jugadores en tu plantilla.</div>';
        return;
    }

    const orden = { 'PORTERO': 0, 'DEFENSA': 1, 'MEDIOCENTRO': 2, 'DELANTERO': 3 };
    const ordenados = [...plantillaData].sort((a, b) => {
        const diff = (orden[a.posicion] ?? 4) - (orden[b.posicion] ?? 4);
        if (diff !== 0) return diff;
        return b.puntosTotal - a.puntosTotal;
    });

    contenedor.innerHTML = ordenados.map(j => {
        const clausulaBloq    = j.clausulaBloqueadaHasta ? new Date(j.clausulaBloqueadaHasta) : null;
        const ahora           = new Date();
        const clausulaBloq2   = clausulaBloq && clausulaBloq > ahora;
        let clausulaHtml      = '';

        if (j.clausula === null) {
            clausulaHtml = '<span class="clausula-badge" style="background:#eee;color:#999">Sin cláusula</span>';
        } else if (clausulaBloq2) {
            const dias = Math.ceil((clausulaBloq - ahora) / (1000 * 60 * 60 * 24));
            clausulaHtml = `<span class="clausula-badge clausula-bloqueada">🔒 ${dias}d</span>`;
        } else {
            clausulaHtml = `<span class="clausula-badge clausula-abierta">✓ ${formatearDinero(j.clausula)}</span>`;
        }

        return `
        <div class="jugador-card">
            <div class="jugador-foto-grande">
                <span>${ICONOS_POSICION[j.posicion] ?? '👤'}</span>
            </div>
            <div class="jugador-card-info">
                <div class="jugador-card-nombre">${j.nombre}</div>
                <div class="jugador-card-equipo">
                    ${j.nombreEquipoReal || 'Sin equipo'} · ${j.posicion.charAt(0) + j.posicion.slice(1).toLowerCase()}
                </div>
                <div class="jugador-card-stats">
                    <div class="jugador-mini-stat">
                        <span class="label">Puntos</span>
                        <span class="valor">${j.puntosTotal}</span>
                    </div>
                    <div class="jugador-mini-stat">
                        <span class="label">Media</span>
                        <span class="valor">${j.mediaPuntos.toFixed(1)}</span>
                    </div>
                    <div class="jugador-mini-stat">
                        <span class="label">Valor</span>
                        <span class="valor">${formatearDinero(j.valorMercado)}</span>
                    </div>
                    <div class="jugador-mini-stat">
                        <span class="label">Cláusula</span>
                        <span class="valor">${clausulaHtml}</span>
                    </div>
                </div>
            </div>
            <div style="position:relative">
                <button class="btn-acciones" onclick="toggleAcciones(event, ${j.id})">
                    Acciones ▾
                </button>
                <div id="acciones-${j.id}" class="acciones-dropdown hidden">
                    <div class="acciones-item" onclick="abrirModalClausula(${j.id}, '${escapar(j.nombre)}')">
                        📈 Subir cláusula
                    </div>
                    <div class="acciones-item" style="color:#aaa;cursor:not-allowed">
                        🛒 Mercado (próximamente)
                    </div>
                </div>
            </div>
        </div>`;
    }).join('');
}

function toggleAcciones(event, jfId) {
    event.stopPropagation();
    const dropdown   = document.getElementById(`acciones-${jfId}`);
    const estaAbierto = !dropdown.classList.contains('hidden');
    cerrarAcciones();
    if (!estaAbierto) {
        dropdown.classList.remove('hidden');
        accionesAbierto = jfId;
    }
}

function cerrarAcciones() {
    document.querySelectorAll('.acciones-dropdown').forEach(d => d.classList.add('hidden'));
    accionesAbierto = null;
}

// ─── Modal subir cláusula ─────────────────────────────────────────────────────
function abrirModalClausula(jfId, nombre) {
    cerrarAcciones();
    jugadorClausulaActual = jfId;
    document.getElementById('clausula-jugador-nombre').textContent = nombre;
    document.getElementById('input-clausula').value                = '';
    document.getElementById('error-clausula').classList.add('hidden');
    document.getElementById('modal-clausula').classList.remove('hidden');
}

function cerrarModalClausula() {
    document.getElementById('modal-clausula').classList.add('hidden');
    jugadorClausulaActual = null;
}

async function confirmarSubirClausula() {
    const cantidad = parseInt(document.getElementById('input-clausula').value);
    if (!cantidad || cantidad <= 0) {
        mostrarErrorElement('error-clausula', 'Introduce una cantidad válida.');
        return;
    }

    try {
        const res = await fetch(
            `/jugadores-fantasy/${jugadorClausulaActual}/clausula?cantidadASubir=${cantidad}&equipoFantasyId=${equipoId}`,
            { method: 'PUT', credentials: 'include' }
        );

        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorElement('error-clausula', texto || 'Error al subir la cláusula.');
            return;
        }

        cerrarModalClausula();
        mostrarToast('✓ Cláusula actualizada');
        await cargarPlantilla();
        renderizarPlantilla();
    } catch (e) {
        mostrarErrorElement('error-clausula', 'Error de conexión.');
    }
}

// ─── TAB PUNTOS ───────────────────────────────────────────────────────────────
async function renderizarPuntos() {
    document.getElementById('jornada-label').textContent = `Jornada ${jornadaActual}`;
    const campo = document.getElementById('campo-puntos');
    campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Cargando...</div>';

    try {
        const res = await fetch(`/equipos-fantasy/${equipoId}/puntos/jornada/${jornadaActual}`, {
            credentials: 'include'
        });

        if (!res.ok) {
            campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Sin datos para esta jornada</div>';
            return;
        }

        const datos = await res.json();
        if (datos.length === 0) {
            campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Sin alineación registrada para esta jornada</div>';
            return;
        }

        renderizarCampoPuntos(campo, datos);
    } catch (e) {
        campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Error al cargar</div>';
    }
}

function renderizarCampoPuntos(campo, datos) {
    campo.innerHTML = '';
    const lineas = parsearFormacion(formacionActual);

    const portero   = datos.find(j => j.posicion === 'PORTERO');
    const defensas  = datos.filter(j => j.posicion === 'DEFENSA');
    const medios    = datos.filter(j => j.posicion === 'MEDIOCENTRO');
    const delant    = datos.filter(j => j.posicion === 'DELANTERO');

    // Portero
    const lineaPortero = document.createElement('div');
    lineaPortero.className = 'linea-campo';
    if (portero) lineaPortero.appendChild(crearCartaPuntos(portero));
    campo.appendChild(lineaPortero);

    // Líneas de campo según formación
    [defensas, medios, delant].forEach((grupo, idx) => {
        const cantidad = lineas[idx] ?? 0;
        const linea    = document.createElement('div');
        linea.className = 'linea-campo';
        grupo.slice(0, cantidad).forEach(j => linea.appendChild(crearCartaPuntos(j)));
        campo.appendChild(linea);
    });
}

function crearCartaPuntos(jugador) {
    const carta = document.createElement('div');
    carta.className = 'carta-jugador';
    carta.innerHTML = `
        <div class="carta-foto">
            <span>${ICONOS_POSICION[jugador.posicion] ?? '👤'}</span>
        </div>
        <div class="carta-nombre">${abreviarNombre(jugador.nombre)}</div>
        <div class="carta-puntos-jornada">${jugador.jugo ? jugador.puntos + ' pts' : '—'}</div>
    `;
    return carta;
}

function cambiarJornada(delta) {
    const nueva = jornadaActual + delta;
    if (nueva < 1) return;
    jornadaActual = nueva;
    renderizarPuntos();
}

// ─── Utilidades ──────────────────────────────────────────────────────────────
function parsearFormacion(f) {
    return f.split('-').map(Number);
}

function abreviarNombre(nombre) {
    if (!nombre) return '—';
    const partes = nombre.trim().split(' ');
    if (partes.length === 1) return partes[0];
    return partes[partes.length - 1];
}

function formatearDinero(cantidad) {
    if (cantidad >= 1000000) return (cantidad / 1000000).toFixed(1) + 'M';
    if (cantidad >= 1000)    return (cantidad / 1000).toFixed(0) + 'K';
    return cantidad.toString();
}

function mostrarToast(texto) {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.style.cssText = 'position:fixed;bottom:80px;left:50%;transform:translateX(-50%);background:#0f3460;color:white;padding:0.6rem 1.2rem;border-radius:20px;font-size:0.85rem;font-weight:600;z-index:200;transition:opacity 0.3s';
        document.body.appendChild(toast);
    }
    toast.textContent    = texto;
    toast.style.opacity  = '1';
    setTimeout(() => { toast.style.opacity = '0'; }, 2500);
}

function mostrarErrorElement(id, texto) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.classList.remove('hidden');
}

function escapar(str) {
    return String(str).replace(/'/g, "\\'").replace(/"/g, '\\"');
}