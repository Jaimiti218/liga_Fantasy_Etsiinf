let equipos = [];
let jornadasData = {};
let resultadoPendiente = null;

document.addEventListener('DOMContentLoaded', async () => {
    await cargarEquipos();
    await cargarTodo();
});

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

async function cargarTodo() {
    const [resJornadas, resPartidos] = await Promise.all([
        fetch('/jornadas', { credentials: 'include' }),
        fetch('/partidos', { credentials: 'include' })
    ]);
    const jornadas = resJornadas.ok ? await resJornadas.json() : [];
    const partidos = resPartidos.ok ? await resPartidos.json() : [];
    jornadasData = {};
    jornadas.forEach(j => { jornadasData[j.numero] = { ...j, partidos: [] }; });
    partidos.forEach(p => {
        if (jornadasData[p.jornada]) jornadasData[p.jornada].partidos.push(p);
    });
    renderizarJornadas();
}

function renderizarJornadas() {
    const contenedor = document.getElementById('lista-jornadas');
    const numeros = Object.keys(jornadasData).sort((a, b) => a - b);
    if (numeros.length === 0) {
        contenedor.innerHTML = '<div class="cargando">No hay jornadas creadas todavía.</div>';
        return;
    }
    contenedor.innerHTML = numeros.map(num => {
        const j = jornadasData[num];
        const fechaStr = j.fechaInicio
            ? new Date(j.fechaInicio).toLocaleString('es-ES', {
                day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
              })
            : 'Sin fecha';
        const partidosHtml = j.partidos.map(p => {
            const pFechaStr = p.fecha
                ? new Date(p.fecha).toLocaleString('es-ES', {
                    day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
                  })
                : 'Sin fecha';
            const resultadoHtml = p.jugado
                ? `<span class="partido-resultado-badge">${p.golesLocal} - ${p.golesVisitante}</span>`
                : '';
            const btnResultado = !p.jugado
                ? `<button class="btn-partido-accion btn-resultado"
                       onclick="abrirModalResultado(${p.id},'${escapar(p.equipoLocalNombre)}','${escapar(p.equipoVisitanteNombre)}',${p.equipoLocalId},${p.equipoVisitanteId})">
                       ⚽ Resultado
                   </button>`
                : '';
            return `
            <div class="partido-admin-card">
                <div class="partido-equipos-admin">
                    ${p.equipoLocalNombre} 🆚 ${p.equipoVisitanteNombre}
                    ${resultadoHtml}
                    <span class="partido-fecha-admin">${pFechaStr}</span>
                </div>
                <div class="partido-acciones-admin">
                    ${btnResultado}
                    <button class="btn-partido-accion btn-editar-partido"
                            onclick="abrirModalEditarPartido(${p.id},${p.equipoLocalId},${p.equipoVisitanteId},'${p.fecha ?? ''}',${num})">
                        ✏️
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
                <div>
                    <span class="jornada-titulo">Jornada ${num}</span>
                    <span style="font-size:0.8rem;opacity:0.75;margin-left:0.75rem">
                        Inicio: ${fechaStr}
                    </span>
                </div>
                <div class="jornada-acciones">
                    <button class="btn-jornada-accion"
                            onclick="abrirModalEditarJornada(${j.id},${num},'${j.fechaInicio ?? ''}')">
                        ✏️ Editar
                    </button>
                    <button class="btn-jornada-accion danger"
                            onclick="eliminarJornada(${j.id},${num})">
                        🗑 Eliminar
                    </button>
                </div>
            </div>
            <div class="partidos-lista">
                ${partidosHtml}
                <button class="btn-add-partido" onclick="abrirModalNuevoPartido(${num})">
                    + Añadir partido a jornada ${num}
                </button>
            </div>
        </div>`;
    }).join('');
}

// ─── Modal jornada ────────────────────────────────────────────────────────────
function abrirModalCrearJornada() {
    document.getElementById('modal-jornada-titulo').textContent = 'Nueva jornada';
    document.getElementById('jornada-num').value = '';
    document.getElementById('jornada-fecha-inicio').value = '';
    document.getElementById('jornada-id-editar').value = '';
    document.getElementById('jornada-num').disabled = false;
    document.getElementById('error-jornada').classList.add('hidden');
    document.getElementById('modal-jornada').classList.remove('hidden');
}

function abrirModalEditarJornada(jornadaId, numero, fechaInicio) {
    document.getElementById('modal-jornada-titulo').textContent = `Editar jornada ${numero}`;
    document.getElementById('jornada-num').value = numero;
    document.getElementById('jornada-num').disabled = true;
    document.getElementById('jornada-id-editar').value = jornadaId;
    document.getElementById('error-jornada').classList.add('hidden');
    if (fechaInicio) {
        const d = new Date(fechaInicio);
        const pad = n => String(n).padStart(2, '0');
        document.getElementById('jornada-fecha-inicio').value =
            `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }
    document.getElementById('modal-jornada').classList.remove('hidden');
}

function cerrarModalJornada() {
    document.getElementById('jornada-num').disabled = false;
    document.getElementById('modal-jornada').classList.add('hidden');
}

async function guardarJornada() {
    const num    = parseInt(document.getElementById('jornada-num').value);
    const fecha  = document.getElementById('jornada-fecha-inicio').value;
    const editId = document.getElementById('jornada-id-editar').value;
    if (!num || num < 1) { mostrarError('error-jornada', 'Introduce un número de jornada válido.'); return; }
    if (!fecha)          { mostrarError('error-jornada', 'Introduce la fecha de inicio.'); return; }
    const body = { numero: num, fechaInicio: fecha };
    try {
        const res = await fetch(editId ? `/jornadas/${editId}` : '/jornadas', {
            method: editId ? 'PUT' : 'POST', credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) { mostrarError('error-jornada', await res.text() || 'Error.'); return; }
        cerrarModalJornada();
        cargarTodo();
    } catch (e) { mostrarError('error-jornada', 'Error de conexión.'); }
}

async function eliminarJornada(jornadaId, numero) {
    if (!confirm(`¿Eliminar jornada ${numero} y todos sus partidos?`)) return;
    const partidos = jornadasData[numero]?.partidos ?? [];
    for (const p of partidos) {
        await fetch(`/partidos/${p.id}`, { method: 'DELETE', credentials: 'include' });
    }
    await fetch(`/jornadas/${jornadaId}`, { method: 'DELETE', credentials: 'include' });
    cargarTodo();
}

// ─── Modal partido ────────────────────────────────────────────────────────────
function abrirModalNuevoPartido(jornada) {
    document.getElementById('modal-partido-titulo').textContent = 'Nuevo partido';
    document.getElementById('partido-id').value = '';
    document.getElementById('partido-jornada').value = jornada;
    document.getElementById('partido-fecha').value = '';
    document.getElementById('error-partido').classList.add('hidden');
    rellenarSelectEquipos('partido-local');
    rellenarSelectEquipos('partido-visitante');
    document.getElementById('modal-partido').classList.remove('hidden');
}

function abrirModalEditarPartido(id, localId, visitanteId, fecha, jornada) {
    document.getElementById('modal-partido-titulo').textContent = 'Editar partido';
    document.getElementById('partido-id').value = id;
    document.getElementById('partido-jornada').value = jornada;
    document.getElementById('error-partido').classList.add('hidden');
    rellenarSelectEquipos('partido-local', localId);
    rellenarSelectEquipos('partido-visitante', visitanteId);
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
    const id      = document.getElementById('partido-id').value;
    const jornada = parseInt(document.getElementById('partido-jornada').value);
    const localId = parseInt(document.getElementById('partido-local').value);
    const visId   = parseInt(document.getElementById('partido-visitante').value);
    const fecha   = document.getElementById('partido-fecha').value;
    if (localId === visId) { mostrarError('error-partido', 'Los dos equipos no pueden ser el mismo.'); return; }
    if (!fecha)            { mostrarError('error-partido', 'La fecha es obligatoria.'); return; }
    const body = { equipoLocalId: localId, equipoVisitanteId: visId, fecha, jornada };
    try {
        const res = await fetch(id ? `/partidos/${id}` : '/partidos', {
            method: id ? 'PUT' : 'POST', credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) { mostrarError('error-partido', await res.text() || 'Error.'); return; }
        cerrarModalPartido();
        cargarTodo();
    } catch (e) { mostrarError('error-partido', 'Error de conexión.'); }
}

async function eliminarPartido(id) {
    if (!confirm('¿Eliminar este partido?')) return;
    const res = await fetch(`/partidos/${id}`, { method: 'DELETE', credentials: 'include' });
    if (res.ok) cargarTodo();
    else alert('Error al eliminar.');
}

// ─── Modal resultado ──────────────────────────────────────────────────────────
async function abrirModalResultado(partidoId, localNombre, visitanteNombre, localId, visitanteId) {
    console.log('abrirModalResultado llamado', partidoId, localNombre, visitanteNombre, localId, visitanteId);
    try {
        document.getElementById('resultado-partido-id').value = partidoId;
        document.getElementById('modal-resultado-titulo').textContent =
            `Resultado: ${localNombre} vs ${visitanteNombre}`;
        document.getElementById('resultado-local-nombre').textContent = localNombre;
        document.getElementById('resultado-visitante-nombre').textContent = visitanteNombre;
        document.getElementById('goles-local').value = 0;
        document.getElementById('goles-visitante').value = 0;
        document.getElementById('error-resultado').classList.add('hidden');

        const contenedor = document.getElementById('lista-jugadores-resultado');
        contenedor.innerHTML = '<div class="cargando">Cargando jugadores...</div>';

        // Abrir el modal YA, antes de cargar jugadores
        document.getElementById('modal-resultado').classList.remove('hidden');

        const resLocal = await fetch(`/equipos/${localId}`, { credentials: 'include' });
        const resVis   = await fetch(`/equipos/${visitanteId}`, { credentials: 'include' });
        const local    = resLocal.ok ? await resLocal.json() : null;
        const vis      = resVis.ok  ? await resVis.json()  : null;

        console.log('equipo local:', local);
        console.log('equipo visitante:', vis);

        const resJugadores = await fetch('/jugadores', { credentials: 'include' });
        const todosJugadores = resJugadores.ok ? await resJugadores.json() : [];

        console.log('total jugadores:', todosJugadores.length);
        console.log('nombre local para filtrar:', local?.name);

        const jugLocal = todosJugadores.filter(j => j.nombreEquipo === local?.name);
        const jugVis   = todosJugadores.filter(j => j.nombreEquipo === vis?.name);

        console.log('jugadores local:', jugLocal.length, 'visitante:', jugVis.length);

        contenedor.innerHTML = renderizarJugadoresResultado(localNombre, jugLocal) +
                               renderizarJugadoresResultado(visitanteNombre, jugVis);

    } catch(e) {
        console.error('Error en abrirModalResultado:', e);
    }
}

function renderizarJugadoresResultado(equipoNombre, jugadores) {
    if (jugadores.length === 0) {
        return `<div style="color:#aaa;padding:0.5rem">Sin jugadores registrados para ${equipoNombre}</div>`;
    }
    return `
    <div class="jugador-resultado-equipo-titulo">
        <span></span>
        <span>${equipoNombre}</span>
        <span title="Posición">Pos.</span>
        <span title="Goles">⚽</span>
        <span title="Asistencias">👟</span>
        <span title="Amarillas">🟨</span>
        <span title="Rojas">🟥</span>
        <span title="Paradas">🧤</span>
    </div>
    ${jugadores.map(j => `
        <div class="jugador-resultado-fila" id="fila-jugador-${j.id}">
            <input type="checkbox" id="jugo-${j.id}" onchange="toggleJugador(${j.id})"/>
            <label for="jugo-${j.id}" style="font-weight:600;cursor:pointer" title="Pos. defecto: ${j.posicion}">${j.fullName}</label>
            <select id="pos-${j.id}" disabled title="Deja vacío para usar posición por defecto (${j.posicion})">
                <option value="">Por defecto</option>
                <option value="PORTERO" ${j.posicion === 'PORTERO' ? 'selected' : ''}>Portero</option>
                <option value="DEFENSA" ${j.posicion === 'DEFENSA' ? 'selected' : ''}>Defensa</option>
                <option value="MEDIOCENTRO" ${j.posicion === 'MEDIOCENTRO' ? 'selected' : ''}>Mediocentro</option>
                <option value="DELANTERO" ${j.posicion === 'DELANTERO' ? 'selected' : ''}>Delantero</option>
            </select>
            <input type="number" id="goles-${j.id}"  min="0" value="0" disabled/>
            <input type="number" id="asist-${j.id}"  min="0" value="0" disabled/>
            <input type="number" id="amari-${j.id}"  min="0" value="0" disabled/>
            <input type="number" id="rojas-${j.id}"  min="0" value="0" disabled/>
            <input type="number" id="parad-${j.id}"  min="0" value="0" disabled
                   ${j.posicion !== 'PORTERO' ? 'style="visibility:hidden"' : ''}/>
        </div>
        <div style="font-size:0.72rem;color:#888;padding:0 0.5rem 0.25rem;font-style:italic">
            Posición por defecto: ${j.posicion}. Cambia solo si jugó en otra posición.
        </div>
    `).join('')}`;
}

function toggleJugador(jugadorId) {
    const jugo = document.getElementById(`jugo-${jugadorId}`).checked;
    ['goles', 'asist', 'amari', 'rojas', 'parad', 'pos'].forEach(campo => {
        const input = document.getElementById(`${campo}-${jugadorId}`);
        if (input) input.disabled = !jugo;
    });
}

function confirmarResultado() {
    const golesLocal = parseInt(document.getElementById('goles-local').value);
    const golesVis   = parseInt(document.getElementById('goles-visitante').value);
    const filas = document.querySelectorAll('[id^="fila-jugador-"]');
    const jugadoresConStats = [];
    filas.forEach(fila => {
        const jId = parseInt(fila.id.replace('fila-jugador-', ''));
        const jugo = document.getElementById(`jugo-${jId}`)?.checked;
        if (!jugo) return;
        jugadoresConStats.push({
            jugadorId: jId,
            jugo: true,
            goles:             parseInt(document.getElementById(`goles-${jId}`)?.value ?? 0),
            asistencias:       parseInt(document.getElementById(`asist-${jId}`)?.value ?? 0),
            tarjetasAmarillas: parseInt(document.getElementById(`amari-${jId}`)?.value ?? 0),
            tarjetasRojas:     parseInt(document.getElementById(`rojas-${jId}`)?.value ?? 0),
            paradas:           parseInt(document.getElementById(`parad-${jId}`)?.value ?? 0),
            posicionPartido:   document.getElementById(`pos-${jId}`)?.value || null
        });
    });
    if (jugadoresConStats.length === 0) {
        mostrarError('error-resultado', 'Marca al menos un jugador que haya jugado.');
        return;
    }
    resultadoPendiente = {
        partidoId: parseInt(document.getElementById('resultado-partido-id').value),
        golesLocal,
        golesVisitante: golesVis,
        estadisticas: jugadoresConStats
    };
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
            alert('Error: ' + await res.text());
            return;
        }
        cerrarModalConfirmar();
        cerrarModalResultado();
        resultadoPendiente = null;
        cargarTodo();
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