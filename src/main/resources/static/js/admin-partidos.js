let equipos = [];
let jornadaActualEditando = null;
let resultadoPendiente = null;

document.addEventListener('DOMContentLoaded', async () => {
    await cargarEquipos();
    await cargarJornadas();
});

// ─── Cargar equipos para selects ──────────────────────────────────────────────
async function cargarEquipos() {
    const res = await fetch('/equipos', { credentials: 'include' });
    if (res.ok) equipos = await res.json();
}

function rellenarSelectEquipos(selectId, equipoSeleccionadoId = null) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = equipos.map(e =>
        `<option value="${e.id}" ${e.id === equipoSeleccionadoId ? 'selected' : ''}>${e.name}</option>`
    ).join('');
}

// ─── Cargar y renderizar jornadas ─────────────────────────────────────────────
async function cargarJornadas() {
    const res = await fetch('/partidos', { credentials: 'include' });
    if (!res.ok) { document.getElementById('lista-jornadas').innerHTML = '<div class="cargando">Error.</div>'; return; }

    const partidos = await res.json();

    // Agrupar por jornada
    const porJornada = {};
    partidos.forEach(p => {
        if (!porJornada[p.jornada]) porJornada[p.jornada] = [];
        porJornada[p.jornada].push(p);
    });

    const contenedor = document.getElementById('lista-jornadas');

    if (Object.keys(porJornada).length === 0) {
        contenedor.innerHTML = '<div class="cargando">No hay jornadas creadas todavía.</div>';
        return;
    }

    contenedor.innerHTML = Object.keys(porJornada)
        .sort((a, b) => a - b)
        .map(jornada => {
            const ps = porJornada[jornada];
            const partidosHtml = ps.map(p => {
                const fechaStr = p.fecha
                    ? new Date(p.fecha).toLocaleString('es-ES', {
                        day: '2-digit', month: 'short',
                        hour: '2-digit', minute: '2-digit'
                      })
                    : 'Sin fecha';

                const resultadoHtml = p.jugado
                    ? `<span class="partido-resultado-badge">${p.golesLocal} - ${p.golesVisitante}</span>`
                    : `<span class="partido-fecha-admin">${fechaStr}</span>`;

                const btnResultado = !p.jugado
                    ? `<button class="btn-partido-accion btn-resultado"
                               onclick="abrirModalResultado(${p.id}, '${escapar(p.equipoLocalNombre)}', '${escapar(p.equipoVisitanteNombre)}', ${p.equipoLocalId}, ${p.equipoVisitanteId})">
                           ⚽ Resultado
                       </button>`
                    : '';

                return `
                <div class="partido-admin-card">
                    <div class="partido-equipos-admin">
                        <span>${p.equipoLocalNombre}</span>
                        ${resultadoHtml}
                        <span>${p.equipoVisitanteNombre}</span>
                    </div>
                    <div class="partido-acciones-admin">
                        ${btnResultado}
                        <button class="btn-partido-accion btn-editar-partido"
                                onclick="abrirModalEditarPartido(${p.id}, ${p.equipoLocalId}, ${p.equipoVisitanteId}, '${p.fecha ?? ''}', ${jornada})">
                            ✏️ Editar
                        </button>
                        <button class="btn-partido-accion btn-eliminar-partido"
                                onclick="eliminarPartido(${p.id})">
                            🗑
                        </button>
                    </div>
                </div>`;
            }).join('');

            return `
            <div class="jornada-bloque">
                <div class="jornada-header">
                    <span class="jornada-titulo">Jornada ${jornada}</span>
                    <div class="jornada-acciones">
                        <button class="btn-jornada-accion danger"
                                onclick="eliminarJornada(${jornada})">
                            🗑 Eliminar jornada
                        </button>
                    </div>
                </div>
                <div class="partidos-lista">
                    ${partidosHtml}
                    <button class="btn-add-partido"
                            onclick="abrirModalNuevoPartido(${jornada})">
                        + Añadir partido a jornada ${jornada}
                    </button>
                </div>
            </div>`;
        }).join('');
}

// ─── Modal crear jornada ──────────────────────────────────────────────────────
function abrirModalCrearJornada() {
    document.getElementById('jornada-num').value = '';
    document.getElementById('jornada-fecha-inicio').value = '';
    document.getElementById('error-jornada').classList.add('hidden');
    document.getElementById('modal-jornada').classList.remove('hidden');
}

function cerrarModalJornada() {
    document.getElementById('modal-jornada').classList.add('hidden');
}

async function guardarJornada() {
    const num    = parseInt(document.getElementById('jornada-num').value);
    const fecha  = document.getElementById('jornada-fecha-inicio').value;

    if (!num || num < 1) {
        mostrarError('error-jornada', 'Introduce un número de jornada válido.');
        return;
    }
    if (!fecha) {
        mostrarError('error-jornada', 'Introduce la fecha de inicio de la jornada.');
        return;
    }

    // Crear un "partido" especial que solo sirve para programar el registro de alineaciones
    // Se crea con esPrimero=true y sin equipos reales — o mejor: simplemente llamar
    // directamente al endpoint de registrar alineaciones en la fecha indicada.
    // La forma más sencilla: crear el primer partido de la jornada con esa fecha.
    cerrarModalJornada();
    abrirModalNuevoPartido(num, fecha);
}

// ─── Modal nuevo/editar partido ───────────────────────────────────────────────
function abrirModalNuevoPartido(jornada, fechaInicioJornada = null) {
    document.getElementById('modal-partido-titulo').textContent = 'Nuevo partido';
    document.getElementById('partido-id').value = '';
    document.getElementById('partido-jornada').value = jornada;
    document.getElementById('partido-fecha').value = fechaInicioJornada ?? '';
    document.getElementById('partido-es-primero').checked = !!fechaInicioJornada;
    document.getElementById('error-partido').classList.add('hidden');
    rellenarSelectEquipos('partido-local');
    rellenarSelectEquipos('partido-visitante');
    document.getElementById('modal-partido').classList.remove('hidden');
}

function abrirModalEditarPartido(id, localId, visitanteId, fecha, jornada) {
    document.getElementById('modal-partido-titulo').textContent = 'Editar partido';
    document.getElementById('partido-id').value = id;
    document.getElementById('partido-jornada').value = jornada;
    document.getElementById('partido-es-primero').checked = false;
    document.getElementById('error-partido').classList.add('hidden');
    rellenarSelectEquipos('partido-local', localId);
    rellenarSelectEquipos('partido-visitante', visitanteId);
    // Convertir fecha ISO a formato datetime-local
    if (fecha) {
        const d = new Date(fecha);
        const pad = n => String(n).padStart(2, '0');
        document.getElementById('partido-fecha').value =
            `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }
    document.getElementById('modal-partido').classList.remove('hidden');
}

function cerrarModalPartido() {
    document.getElementById('modal-partido').classList.add('hidden');
}

async function guardarPartido() {
    const id       = document.getElementById('partido-id').value;
    const jornada  = parseInt(document.getElementById('partido-jornada').value);
    const localId  = parseInt(document.getElementById('partido-local').value);
    const visId    = parseInt(document.getElementById('partido-visitante').value);
    const fecha    = document.getElementById('partido-fecha').value;
    const esPrimero = document.getElementById('partido-es-primero').checked;

    if (localId === visId) {
        mostrarError('error-partido', 'Los dos equipos no pueden ser el mismo.');
        return;
    }
    if (!fecha) {
        mostrarError('error-partido', 'La fecha es obligatoria.');
        return;
    }

    const body = {
        equipoLocalId: localId,
        equipoVisitanteId: visId,
        fecha: fecha,
        jornada: jornada,
        esPrimero: esPrimero
    };

    try {
        let res;
        if (id) {
            res = await fetch(`/partidos/${id}`, {
                method: 'PUT', credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
        } else {
            res = await fetch('/partidos', {
                method: 'POST', credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
        }

        if (!res.ok) {
            const txt = await res.text();
            mostrarError('error-partido', txt || 'Error al guardar.');
            return;
        }
        cerrarModalPartido();
        cargarJornadas();
    } catch (e) {
        mostrarError('error-partido', 'Error de conexión.');
    }
}

async function eliminarPartido(id) {
    if (!confirm('¿Eliminar este partido?')) return;
    const res = await fetch(`/partidos/${id}`, {
        method: 'DELETE', credentials: 'include'
    });
    if (res.ok) cargarJornadas();
    else alert('Error al eliminar el partido.');
}

async function eliminarJornada(jornada) {
    if (!confirm(`¿Eliminar todos los partidos de la jornada ${jornada}?`)) return;
    const res = await fetch('/partidos', { credentials: 'include' });
    const partidos = await res.json();
    const deEstaJornada = partidos.filter(p => p.jornada === jornada);
    for (const p of deEstaJornada) {
        await fetch(`/partidos/${p.id}`, { method: 'DELETE', credentials: 'include' });
    }
    cargarJornadas();
}

// ─── Modal resultado ──────────────────────────────────────────────────────────
async function abrirModalResultado(partidoId, localNombre, visitanteNombre, localId, visitanteId) {
    document.getElementById('resultado-partido-id').value = partidoId;
    document.getElementById('modal-resultado-titulo').textContent =
        `Resultado: ${localNombre} vs ${visitanteNombre}`;
    document.getElementById('resultado-local-nombre').textContent = localNombre;
    document.getElementById('resultado-visitante-nombre').textContent = visitanteNombre;
    document.getElementById('goles-local').value = 0;
    document.getElementById('goles-visitante').value = 0;
    document.getElementById('error-resultado').classList.add('hidden');

    // Cargar jugadores de ambos equipos
    const contenedor = document.getElementById('lista-jugadores-resultado');
    contenedor.innerHTML = '<div class="cargando">Cargando jugadores...</div>';

    const resLocal = await fetch(`/equipos/${localId}`, { credentials: 'include' });
    const resVis   = await fetch(`/equipos/${visitanteId}`, { credentials: 'include' });
    const local    = resLocal.ok ? await resLocal.json() : null;
    const vis      = resVis.ok  ? await resVis.json()  : null;

    // Obtener jugadores de cada equipo
    const resJugadores = await fetch('/jugadores', { credentials: 'include' });
    const todosJugadores = resJugadores.ok ? await resJugadores.json() : [];

    const jugLocal = todosJugadores.filter(j => j.nombreEquipo === local?.name);
    const jugVis   = todosJugadores.filter(j => j.nombreEquipo === vis?.name);

    contenedor.innerHTML = renderizarJugadoresResultado(localNombre, jugLocal) +
                           renderizarJugadoresResultado(visitanteNombre, jugVis);

    document.getElementById('modal-resultado').classList.remove('hidden');
}

function renderizarJugadoresResultado(equipoNombre, jugadores) {
    if (jugadores.length === 0) return `<div style="color:#aaa;padding:0.5rem">Sin jugadores registrados para ${equipoNombre}</div>`;

    return `
    <div class="jugador-resultado-equipo-titulo">
        <span></span>
        <span>${equipoNombre}</span>
        <span title="Goles">⚽</span>
        <span title="Asistencias">👟</span>
        <span title="Amarillas">🟨</span>
        <span title="Rojas">🟥</span>
        <span title="Paradas">🧤</span>
    </div>
    ${jugadores.map(j => `
        <div class="jugador-resultado-fila" id="fila-jugador-${j.id}">
            <input type="checkbox" id="jugo-${j.id}" onchange="toggleJugador(${j.id})"/>
            <label for="jugo-${j.id}" style="font-weight:600;cursor:pointer">${j.fullName}</label>
            <input type="number" id="goles-${j.id}"   min="0" value="0" disabled/>
            <input type="number" id="asist-${j.id}"   min="0" value="0" disabled/>
            <input type="number" id="amari-${j.id}"   min="0" value="0" disabled/>
            <input type="number" id="rojas-${j.id}"   min="0" value="0" disabled/>
            <input type="number" id="parad-${j.id}"   min="0" value="0" disabled
                   ${j.posicion !== 'PORTERO' ? 'style="visibility:hidden"' : ''}/>
        </div>
    `).join('')}`;
}

function toggleJugador(jugadorId) {
    const jugo = document.getElementById(`jugo-${jugadorId}`).checked;
    ['goles', 'asist', 'amari', 'rojas', 'parad'].forEach(campo => {
        const input = document.getElementById(`${campo}-${jugadorId}`);
        if (input) input.disabled = !jugo;
    });
}

function confirmarResultado() {
    const golesLocal = parseInt(document.getElementById('goles-local').value);
    const golesVis   = parseInt(document.getElementById('goles-visitante').value);

    // Recoger jugadores que jugaron
    const filas = document.querySelectorAll('[id^="fila-jugador-"]');
    const jugadoresConStats = [];
    filas.forEach(fila => {
        const jId = parseInt(fila.id.replace('fila-jugador-', ''));
        const jugo = document.getElementById(`jugo-${jId}`)?.checked;
        if (!jugo) return;
        jugadoresConStats.push({
            jugadorId: jId,
            jugo: true,
            goles:            parseInt(document.getElementById(`goles-${jId}`)?.value ?? 0),
            asistencias:      parseInt(document.getElementById(`asist-${jId}`)?.value ?? 0),
            tarjetasAmarillas: parseInt(document.getElementById(`amari-${jId}`)?.value ?? 0),
            tarjetasRojas:    parseInt(document.getElementById(`rojas-${jId}`)?.value ?? 0),
            paradas:          parseInt(document.getElementById(`parad-${jId}`)?.value ?? 0)
        });
    });

    if (jugadoresConStats.length === 0) {
        mostrarError('error-resultado', 'Marca al menos un jugador que haya jugado.');
        return;
    }

    // Guardar datos para confirmación
    resultadoPendiente = {
        partidoId: parseInt(document.getElementById('resultado-partido-id').value),
        golesLocal, golesVisitante: golesVis,
        estadisticas: jugadoresConStats
    };

    // Mostrar resumen
    const resumen = document.getElementById('resumen-confirmacion');
    resumen.innerHTML = `
        <strong>Resultado:</strong>
        ${document.getElementById('resultado-local-nombre').textContent} ${golesLocal} –
        ${golesVis} ${document.getElementById('resultado-visitante-nombre').textContent}
        <br><strong>Jugadores con stats:</strong> ${jugadoresConStats.length}
        <br>${jugadoresConStats.map(j => {
            const nombre = document.querySelector(`#fila-jugador-${j.jugadorId} label`)?.textContent ?? j.jugadorId;
            return `${nombre}: ${j.goles}⚽ ${j.asistencias}👟 ${j.tarjetasAmarillas}🟨 ${j.tarjetasRojas}🟥`;
        }).join('<br>')}
    `;

    document.getElementById('modal-confirmar').classList.remove('hidden');
}

async function ejecutarGuardarResultado() {
    if (!resultadoPendiente) return;
    try {
        const res = await fetch(`/partidos/${resultadoPendiente.partidoId}/resultado`, {
            method: 'PUT', credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(resultadoPendiente)
        });
        if (!res.ok) {
            const txt = await res.text();
            alert('Error: ' + txt);
            return;
        }
        cerrarModalConfirmar();
        cerrarModalResultado();
        resultadoPendiente = null;
        cargarJornadas();
        alert('✓ Resultado guardado y puntos otorgados correctamente.');
    } catch (e) {
        alert('Error de conexión.');
    }
}

function cerrarModalResultado() {
    document.getElementById('modal-resultado').classList.add('hidden');
}

function cerrarModalConfirmar() {
    document.getElementById('modal-confirmar').classList.add('hidden');
}

// ─── Utilidades ───────────────────────────────────────────────────────────────
function mostrarError(id, texto) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.classList.remove('hidden');
}

function escapar(str) {
    return String(str).replace(/'/g, "\\'").replace(/"/g, '\\"');
}