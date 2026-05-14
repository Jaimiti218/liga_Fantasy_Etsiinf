// ─── Estado global ──────────────────────────────────────────────────────────
let equipoId              = null;
let ligaId                = null;
let plantillaData         = [];
let alineacionActual      = [];
let formacionActual       = '2-3-1';
let posicionSeleccionada  = null;
let candidatoSeleccionado = null;
let jornadaActual         = 1;
let accionesAbierto       = null;
let jugadorClausulaActual = null;
const MAX_JUGADORES       = 13;
let jugadorVentaActual = null;

const ICONOS_POSICION = {
    'PORTERO':     '🧤',
    'DEFENSA':     '🛡',
    'MEDIOCENTRO': '⚙️',
    'DELANTERO':   '⚡'
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
        const resEquipo = await fetch(`/equipos-fantasy/${equipoId}`, { credentials: 'include' });
        if (resEquipo.ok) {
            const equipo    = await resEquipo.json();
            formacionActual = equipo.formacion || '2-3-1';
            window.dineroEquipo = equipo.dinero;
            document.getElementById('select-formacion').value = formacionActual;
        }

        const res = await fetch(`/equipos-fantasy/${equipoId}/plantilla`, { credentials: 'include' });
        if (!res.ok) { plantillaData = []; return; }
        plantillaData = await res.json();

        const alineados    = plantillaData.filter(j => j.alineado);
        const porteroAlin  = alineados.filter(j => j.posicion === 'PORTERO');
        const defensasAlin = alineados.filter(j => j.posicion === 'DEFENSA');
        const mediosAlin   = alineados.filter(j => j.posicion === 'MEDIOCENTRO');
        const delantAlin   = alineados.filter(j => j.posicion === 'DELANTERO');

        const lineas = parsearFormacion(formacionActual);
        alineacionActual = [
            porteroAlin[0]?.id ?? null,
            ...rellenarLinea(defensasAlin, lineas[0]),
            ...rellenarLinea(mediosAlin,   lineas[1]),
            ...rellenarLinea(delantAlin,   lineas[2])
        ];

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

    renderizarLinea('linea-portero', [alineacionActual[0] ?? null], 0, 'PORTERO');

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

    const idActual    = alineacionActual[posicion] ?? null;
    const disponibles = plantillaData.filter(j => {
        if (j.posicion !== posicionLinea) return false;
        if (alineacionActual.includes(j.id) && j.id !== idActual) return false;
        if (j.id === idActual) return false;
        return true;
    });

    let html = '';
    if (idActual !== null) {
        html += `<div class="candidato-item candidato-quitar" onclick="quitarJugadorPosicion()">✕ Quitar jugador</div>`;
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

    document.getElementById('lista-candidatos').innerHTML = html;
    document.getElementById('panel-seleccion').classList.remove('hidden');
}

function quitarJugadorPosicion() {
    if (posicionSeleccionada === null) return;
    const nueva = [...alineacionActual];
    nueva[posicionSeleccionada] = null;
    alineacionActual = nueva;
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
    const nueva = [...alineacionActual];
    while (nueva.length < 7) nueva.push(null);
    nueva[posicionSeleccionada] = candidatoSeleccionado;
    alineacionActual = nueva;
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
    const portero          = alineacionActual[0] ?? null;
    const lineas           = parsearFormacion(nuevaFormacion);
    const defensasActuales = obtenerAlineadosDePosicion('DEFENSA');
    const mediosActuales   = obtenerAlineadosDePosicion('MEDIOCENTRO');
    const delantActuales   = obtenerAlineadosDePosicion('DELANTERO');

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
            body: JSON.stringify({ jugadorFantasyIds: ids, formacion: formacionActual })
        });

        if (!res.ok) {
            const texto = await res.text();
            alert(texto || 'Error al guardar la alineación.');
            return;
        }

        mostrarToast('✓ Alineación guardada');
        actualizarResumen();
        renderizarCampo();
    } catch (e) {
        alert('Error de conexión.');
    }
}

// ─── Resumen ──────────────────────────────────────────────────────────────────
function actualizarResumen() {
    const valorTotal = plantillaData.reduce((sum, j) => sum + j.valorMercado, 0);
    document.getElementById('valor-equipo').textContent     = formatearDinero(valorTotal);
    document.getElementById('num-jugadores').textContent    = `${plantillaData.length} / ${MAX_JUGADORES}`;
    document.getElementById('formacion-actual').textContent = formacionActual;
}

// ─── TAB PLANTILLA ────────────────────────────────────────────────────────────
async function renderizarPlantilla() {
    const contenedor = document.getElementById('lista-plantilla');
    contenedor.innerHTML = '<div class="cargando-ligas">Cargando...</div>';

    if (plantillaData.length === 0) {
        contenedor.innerHTML = '<div class="cargando-ligas">No tienes jugadores en tu plantilla.</div>';
        return;
    }

    const orden    = { 'PORTERO': 0, 'DEFENSA': 1, 'MEDIOCENTRO': 2, 'DELANTERO': 3 };
    const ordenados = [...plantillaData].sort((a, b) => {
        const diff = (orden[a.posicion] ?? 4) - (orden[b.posicion] ?? 4);
        return diff !== 0 ? diff : b.puntosTotal - a.puntosTotal;
    });

    // Cargar jornadas de todos en paralelo
    const jornadasMap = {};
    await Promise.all(ordenados.map(async j => {
        jornadasMap[j.id] = await cargarJornadasJugador(j.id);
    }));

    contenedor.innerHTML = ordenados.map(j => {
        const jornadas  = jornadasMap[j.id] ?? [];
        const ultimas5  = jornadas.slice(-5);
        while (ultimas5.length < 5) ultimas5.unshift(null);

        const jornadasHtml = ultimas5.map(jorn => {
            if (!jorn) return `<div class="jornada-mini jornada-vacia">—</div>`;
            const color = jorn.puntos >= 8 ? 'alta' : jorn.puntos >= 4 ? 'media' : 'baja';
            return `<div class="jornada-mini jornada-${color}" onclick="event.stopPropagation(); abrirDetalleJugador(${j.id})">
                J${jorn.jornada}<br><strong>${jorn.jugo ? jorn.puntos : '—'}</strong>
            </div>`;
        }).join('');

        const clausulaBloq  = j.clausulaBloqueadaHasta ? new Date(j.clausulaBloqueadaHasta) : null;
        const ahora         = new Date();
        const bloqueada     = clausulaBloq && clausulaBloq > ahora;
        let clausulaHtml    = '';

        if (j.clausula === null) {
            clausulaHtml = '<span class="clausula-badge clausula-sin">Sin cláusula</span>';
        } else if (bloqueada) {
            const diff   = clausulaBloq - ahora;
            const dias   = Math.floor(diff / (1000 * 60 * 60 * 24));
            const horas  = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const tiempo = dias > 0 ? `${dias}d ${horas}h` : `${horas}h`;
            clausulaHtml = `
                <span class="clausula-badge clausula-bloqueada">🔒 ${tiempo}</span>
                <span class="clausula-badge clausula-valor-bloqueada">${formatearDinero(j.clausula)}</span>
            `;
        } else {
            clausulaHtml = `<span class="clausula-badge clausula-abierta">✓ ${formatearDinero(j.clausula)}</span>`;
        }

        return `
        <div class="jugador-card" onclick="abrirDetalleJugador(${j.id})">
            <div class="jugador-foto-grande">
                <span>${ICONOS_POSICION[j.posicion] ?? '👤'}</span>
            </div>
            <div class="jugador-card-info">
                <div class="jugador-card-nombre">${j.nombre}</div>
                <div class="jugador-card-equipo">
                    <span class="badge-equipo">${j.nombreEquipoReal || 'Sin equipo'}</span>
                    <span class="badge-posicion ${j.posicion}">${j.posicion.charAt(0) + j.posicion.slice(1).toLowerCase()}</span>
                </div>
                <div class="jornadas-recientes">${jornadasHtml}</div>
            </div>
            <div class="jugador-card-derecha">
                <div class="jugador-card-stats">
                    <div class="jugador-mini-stat">
                        <span class="label">Pts</span>
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
                <div onclick="event.stopPropagation()" style="position:relative">
                    ${j.enVenta
                        ? `<button class="btn-quitar-mercado" onclick="quitarDelMercado(${j.id})">
                            🔴 Quitar
                        </button>`
                        : `<button class="btn-acciones" onclick="toggleAcciones(event, ${j.id})">
                            Acciones ▾
                        </button>
                        <div id="acciones-${j.id}" class="acciones-dropdown hidden">
                            <div class="acciones-item" onclick="abrirModalClausula(${j.id}, '${escapar(j.nombre)}')">
                                📈 Subir cláusula
                            </div>
                            <div class="acciones-item" onclick="ponerEnVentaDesde(${j.id})">
                                🛒 Añadir al mercado
                            </div>
                        </div>`
                    }
                </div>
            </div>
        </div>`;
    }).join('');
}


function ponerEnVentaDesde(jugadorFantasyId) {
    cerrarAcciones();
    const jugador = plantillaData.find(j => j.id === jugadorFantasyId);
    if (!jugador) return;

    jugadorVentaActual = jugadorFantasyId;
    document.getElementById('venta-jugador-nombre').textContent    = jugador.nombre;
    document.getElementById('venta-valor-mercado').textContent     = formatearDinero(jugador.valorMercado);
    document.getElementById('venta-clausula').textContent          = jugador.clausula ? formatearDinero(jugador.clausula) : 'Sin cláusula';
    document.getElementById('venta-saldo').textContent             = formatearDinero(window.dineroEquipo ?? 0);
    document.getElementById('modal-confirmar-venta').classList.remove('hidden');
}

function cerrarModalVenta() {
    document.getElementById('modal-confirmar-venta').classList.add('hidden');
    jugadorVentaActual = null;
}

async function confirmarPonerEnVenta() {
    if (!jugadorVentaActual) return;
    try {
        const res = await fetch(`/mercado/vender/${jugadorVentaActual}`, {
            method: 'POST', credentials: 'include'
        });
        if (!res.ok) { alert('Error al poner en venta.'); return; }
        cerrarModalVenta();
        mostrarToast('✓ Jugador puesto en venta');
        await cargarPlantilla();
        renderizarPlantilla();
    } catch (e) {
        alert('Error de conexión.');
    }
}

function toggleAcciones(event, jfId) {
    event.stopPropagation();
    const dropdown    = document.getElementById(`acciones-${jfId}`);
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
    document.getElementById('clausula-saldo').textContent          = formatearDinero(window.dineroEquipo ?? 0);
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
    if (!confirm(`¿Seguro que quieres invertir ${formatearDinero(cantidad)}? La cláusula subirá ${formatearDinero(cantidad * 2)}.`)) return;

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

// ─── Detalle jugador ──────────────────────────────────────────────────────────
async function cargarJornadasJugador(jugadorFantasyId) {
    try {
        const res = await fetch(`/equipos-fantasy/jugador/${jugadorFantasyId}/jornadas`, {
            credentials: 'include'
        });
        if (!res.ok) return [];
        return await res.json();
    } catch (e) {
        return [];
    }
}

async function abrirDetalleJugador(jugadorFantasyId) {
    const jugador = plantillaData.find(j => j.id === jugadorFantasyId);
    if (!jugador) return;

    const jornadas = await cargarJornadasJugador(jugadorFantasyId);

    document.getElementById('detalle-icono').textContent           = ICONOS_POSICION[jugador.posicion] ?? '👤';
    document.getElementById('detalle-nombre').textContent          = jugador.nombre;
    document.getElementById('detalle-equipo').textContent          = jugador.nombreEquipoReal || 'Sin equipo';
    document.getElementById('detalle-posicion-badge').textContent  = jugador.posicion.charAt(0) + jugador.posicion.slice(1).toLowerCase();
    document.getElementById('detalle-posicion-badge').className    = `badge-posicion ${jugador.posicion}`;
    document.getElementById('detalle-puntos').textContent          = jugador.puntosTotal;
    document.getElementById('detalle-media').textContent           = jugador.mediaPuntos.toFixed(2);
    document.getElementById('detalle-valor').textContent           = formatearDinero(jugador.valorMercado);

    window._jornadasDetalle      = jornadas;
    window._jornadaDetalleActual = jornadas.length > 0 ? jornadas.length - 1 : 0;
    window._jugadorDetalleActual = jugador;

    if (jornadas.length === 0) {
        document.getElementById('detalle-jornada-label').textContent  = 'Sin jornadas';
        document.getElementById('detalle-jornada-puntos').textContent = '—';
        document.getElementById('detalle-stats-tabla').innerHTML      =
            '<div class="sin-datos">Sin jornadas jugadas todavía</div>';
    } else {
        renderizarJornadaDetalle();
    }

    document.getElementById('modal-detalle-jugador').classList.remove('hidden');
}

function renderizarJornadaDetalle() {
    const jornadas = window._jornadasDetalle;
    const jugador  = window._jugadorDetalleActual;
    const idx      = window._jornadaDetalleActual;
    const jorn     = jornadas[idx];

    document.getElementById('detalle-jornada-label').textContent  = `Jornada ${jorn.jornada}`;
    document.getElementById('detalle-jornada-puntos').textContent = jorn.jugo ? jorn.puntos : '—';

    const stats = document.getElementById('detalle-stats-tabla');
    if (!jorn.jugo) {
        stats.innerHTML = '<div class="sin-datos">No jugó en esta jornada</div>';
        return;
    }

    const esPortero = jugador?.posicion === 'PORTERO';
    stats.innerHTML = `
        <div class="stat-fila">
            <span class="stat-cantidad">${jorn.goles}</span>
            <span class="stat-nombre">Goles</span>
            <span class="stat-puntos ${jorn.goles > 0 ? 'positivo' : 'neutro'}">${jorn.goles * 4}</span>
        </div>
        <div class="stat-fila">
            <span class="stat-cantidad">${jorn.asistencias}</span>
            <span class="stat-nombre">Asistencias</span>
            <span class="stat-puntos ${jorn.asistencias > 0 ? 'positivo' : 'neutro'}">${jorn.asistencias * 2}</span>
        </div>
        <div class="stat-fila">
            <span class="stat-cantidad">${jorn.tarjetasAmarillas}</span>
            <span class="stat-nombre">Tarjetas amarillas</span>
            <span class="stat-puntos ${jorn.tarjetasAmarillas > 0 ? 'negativo' : 'neutro'}">${jorn.tarjetasAmarillas * -1}</span>
        </div>
        <div class="stat-fila">
            <span class="stat-cantidad">${jorn.tarjetasRojas}</span>
            <span class="stat-nombre">Tarjetas rojas</span>
            <span class="stat-puntos ${jorn.tarjetasRojas > 0 ? 'negativo' : 'neutro'}">${jorn.tarjetasRojas * -3}</span>
        </div>
        ${esPortero ? `
        <div class="stat-fila">
            <span class="stat-cantidad">${jorn.paradas}</span>
            <span class="stat-nombre">Paradas</span>
            <span class="stat-puntos ${jorn.paradas > 0 ? 'positivo' : 'neutro'}">${jorn.paradas}</span>
        </div>` : ''}
    `;
}

function cambiarJornadaDetalle(delta) {
    const nueva = window._jornadaDetalleActual + delta;
    if (nueva < 0 || nueva >= window._jornadasDetalle.length) return;
    window._jornadaDetalleActual = nueva;
    renderizarJornadaDetalle();
}

function cerrarDetalleJugador() {
    document.getElementById('modal-detalle-jugador').classList.add('hidden');
}

// ─── TAB PUNTOS ───────────────────────────────────────────────────────────────
async function renderizarPuntos() {
    document.getElementById('jornada-label').textContent = `Jornada ${jornadaActual}`;
    const campo = document.getElementById('campo-puntos');
    campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Cargando...</div>';

    try {
        const res = await fetch(`/equipos-fantasy/${equipoId}/puntos/jornada/${jornadaActual}/info`, {
            credentials: 'include'
        });

        if (!res.ok) {
            campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Sin datos para esta jornada</div>';
            return;
        }

        const datos = await res.json();

        if (!datos.jugadores || datos.jugadores.length === 0) {
            campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Sin alineación registrada para esta jornada</div>';
            return;
        }

        // Usar la formación guardada para esa jornada, no la actual
        const formacionJornada = datos.formacion || formacionActual;
        renderizarCampoPuntos(campo, datos.jugadores, formacionJornada);

    } catch (e) {
        campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Error al cargar</div>';
    }
}

function renderizarCampoPuntos(campo, datos) {
    campo.innerHTML = '';
    const lineas = parsearFormacion(formacion);
    const portero  = datos.find(j => j.posicion === 'PORTERO');
    const defensas = datos.filter(j => j.posicion === 'DEFENSA');
    const medios   = datos.filter(j => j.posicion === 'MEDIOCENTRO');
    const delant   = datos.filter(j => j.posicion === 'DELANTERO');

    const lineaPortero = document.createElement('div');
    lineaPortero.className = 'linea-campo';
    if (portero) lineaPortero.appendChild(crearCartaPuntos(portero));
    campo.appendChild(lineaPortero);

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
    carta.style.cursor = 'pointer';
    carta.onclick = () => {
        const jf = plantillaData.find(j => j.nombre === jugador.nombre);
        if (jf) abrirDetalleJugador(jf.id);
    };
    carta.innerHTML = `
        <div class="carta-foto"><span>${ICONOS_POSICION[jugador.posicion] ?? '👤'}</span></div>
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
function parsearFormacion(f) { return f.split('-').map(Number); }

function abreviarNombre(nombre) {
    if (!nombre) return '—';
    const partes = nombre.trim().split(' ');
    return partes.length === 1 ? partes[0] : partes[partes.length - 1];
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
    toast.textContent   = texto;
    toast.style.opacity = '1';
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

function irAMercado() {
    window.location.href = `/fantasy/mercado/${ligaId}`;
}

async function quitarDelMercado(jugadorFantasyId) {
    if (!confirm('¿Quieres quitar este jugador del mercado?')) return;
    try {
        const res = await fetch(`/mercado/vender/${jugadorFantasyId}`, {
            method: 'DELETE', credentials: 'include'
        });
        if (!res.ok) { alert('Error al quitar del mercado.'); return; }
        mostrarToast('✓ Jugador retirado del mercado');
        await cargarPlantilla();
        renderizarPlantilla();
    } catch (e) {
        alert('Error de conexión.');
    }
}