let equipoOtroId  = null;
let ligaId        = null;
let miEquipoId    = null;
let miDinero      = 0;
let plantillaOtro = [];
let jornadaActual = 1;
let jugadorAccionId   = null;
let jugadorClausulaId = null;

const ICONOS_POSICION = {
    'PORTERO': '🧤', 'DEFENSA': '🛡',
    'MEDIOCENTRO': '⚙️', 'DELANTERO': '⚡'
};

document.addEventListener('DOMContentLoaded', async () => {
    const partes  = window.location.pathname.split('/');
    equipoOtroId  = parseInt(partes[partes.length - 1]);
    ligaId        = parseInt(new URLSearchParams(window.location.search).get('liga'));

    if (!equipoOtroId || !ligaId) { window.location.href = '/fantasy/mis-ligas'; return; }

    await cargarDatosMiEquipo();
    await cargarInfoEquipoOtro();
    await cargarPlantillaOtro();
    cambiarTab('plantilla');
});

function volverALiga()    { window.location.href = `/fantasy/liga/${ligaId}`; }
function irAMiPlantilla() { window.location.href = `/fantasy/plantilla/${miEquipoId}?liga=${ligaId}`; }
function irAMercado()     { window.location.href = `/fantasy/mercado/${ligaId}`; }

async function cargarDatosMiEquipo() {
    try {
        const res = await fetch(`/equipos-fantasy/liga/${ligaId}/mi-equipo`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        const datos = await res.json();
        miEquipoId = datos.id;
        miDinero   = datos.dinero;
    } catch (e) {}
}

async function cargarInfoEquipoOtro() {
    try {
        const res = await fetch(`/equipos-fantasy/${equipoOtroId}`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        const equipo = await res.json();

        document.getElementById('perfil-nombre').textContent  = equipo.userNombre;
        document.getElementById('perfil-puntos').textContent  = equipo.puntos + ' pts';
        document.getElementById('perfil-dinero').textContent  = formatearDinero(equipo.dinero);

        // Foto de perfil — necesitaría endpoint de usuario, por ahora icono por defecto
        document.getElementById('perfil-avatar').innerHTML = '👤';

    } catch (e) {}
}

async function cargarPlantillaOtro() {
    try {
        const res = await fetch(`/equipos-fantasy/${equipoOtroId}/plantilla-publica`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        plantillaOtro = await res.json();
    } catch (e) { plantillaOtro = []; }
}

function cambiarTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-seccion').forEach(s => s.classList.add('hidden'));
    document.getElementById(`tab-${tab}`).classList.add('active');
    document.getElementById(`seccion-${tab}`).classList.remove('hidden');

    if (tab === 'plantilla') renderizarPlantillaOtro();
    if (tab === 'puntos')    renderizarPuntosOtro();
}

async function renderizarPlantillaOtro() {
    const contenedor = document.getElementById('lista-plantilla-otro');
    contenedor.innerHTML = '<div class="cargando-ligas">Cargando...</div>';

    if (plantillaOtro.length === 0) {
        contenedor.innerHTML = '<div class="cargando-ligas">Este equipo no tiene jugadores.</div>';
        return;
    }

    const orden    = { 'PORTERO': 0, 'DEFENSA': 1, 'MEDIOCENTRO': 2, 'DELANTERO': 3 };
    const ordenados = [...plantillaOtro].sort((a, b) => {
        const diff = (orden[a.posicion] ?? 4) - (orden[b.posicion] ?? 4);
        return diff !== 0 ? diff : b.puntosTotal - a.puntosTotal;
    });

    const jornadasMap = {};
    await Promise.all(ordenados.map(async j => {
        jornadasMap[j.id] = await cargarJornadasJugador(j.id);
    }));

    contenedor.innerHTML = ordenados.map(j => {
        const jornadas = jornadasMap[j.id] ?? [];
        const ultimas5 = jornadas.slice(-5);
        while (ultimas5.length < 5) ultimas5.unshift(null);

        const jornadasHtml = ultimas5.map(jorn => {
            if (!jorn) return `<div class="jornada-mini jornada-vacia">—</div>`;
            const color = jorn.puntos >= 8 ? 'alta' : jorn.puntos >= 4 ? 'media' : 'baja';
            return `<div class="jornada-mini jornada-${color}">
                J${jorn.jornada}<br><strong>${jorn.jugo ? jorn.puntos : '—'}</strong>
            </div>`;
        }).join('');

        // Estado de la cláusula
        const clausulaBloq = j.clausulaBloqueadaHasta ? new Date(j.clausulaBloqueadaHasta) : null;
        const ahora        = new Date();
        const bloqueada    = clausulaBloq && clausulaBloq > ahora;

        // Badge de cláusula para las stats (igual que en mi plantilla)
        let clausulaHtml = '';
        if (j.clausula === null || j.clausula === undefined) {
            clausulaHtml = '<span class="clausula-badge" style="background:#eee;color:#999">Sin cláusula</span>';
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

        // Texto y estilo para el botón de cláusula en el dropdown
        const clausulaEstilo = bloqueada ? 'color:#aaa;cursor:not-allowed' : 'color:#333';
        const clausulaTexto  = j.clausula
            ? (bloqueada
                ? `🔒 Cláusula bloqueada (${Math.ceil((clausulaBloq - ahora) / 86400000)}d)`
                : `✓ Pagar cláusula: ${formatearDinero(j.clausula)}`)
            : 'Sin cláusula';

        return `
        <div class="jugador-card" onclick="abrirDetalleJugador(${j.id})">
            <div class="jugador-foto-grande">
                <span>${ICONOS_POSICION[j.posicion] ?? '👤'}</span>
            </div>
            <div class="jugador-card-info">
                <div class="jugador-card-nombre">${j.nombre}</div>
                <div class="jugador-card-equipo">
                    <span class="badge-equipo">${j.nombreEquipoReal || 'Sin equipo'}</span>
                    <span class="badge-posicion ${j.posicion}">
                        ${j.posicion.charAt(0) + j.posicion.slice(1).toLowerCase()}
                    </span>
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
                    <button class="btn-acciones" onclick="toggleAccionesOtro(event, ${j.id})">
                        Acciones ▾
                    </button>
                    <div id="acciones-otro-${j.id}" class="acciones-dropdown hidden">
                        <div class="acciones-item"
                             onclick="abrirOfertaCompra(${j.id}, '${escapar(j.nombre)}')">
                            💰 Oferta de compra
                        </div>
                        <div class="acciones-item"
                             style="${clausulaEstilo}"
                             onclick="${!bloqueada && j.clausula ? `abrirPagarClausula(${j.id}, '${escapar(j.nombre)}', ${j.clausula})` : ''}">
                            ${clausulaTexto}
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
    }).join('');
}

function toggleAccionesOtro(event, jfId) {
    event.stopPropagation();
    const dropdown    = document.getElementById(`acciones-otro-${jfId}`);
    const estaAbierto = !dropdown.classList.contains('hidden');
    document.querySelectorAll('.acciones-dropdown').forEach(d => d.classList.add('hidden'));
    if (!estaAbierto) dropdown.classList.remove('hidden');
}

// ─── Oferta de compra ─────────────────────────────────────────────────────────
function abrirOfertaCompra(jfId, nombre) {
    jugadorAccionId = jfId;
    document.getElementById('oferta-jugador-nombre').textContent = nombre;
    document.getElementById('oferta-saldo').textContent          = formatearDinero(miDinero);
    document.getElementById('input-oferta').value                = '';
    document.getElementById('error-oferta').classList.add('hidden');
    document.querySelectorAll('.acciones-dropdown').forEach(d => d.classList.add('hidden'));
    document.getElementById('modal-oferta-compra').classList.remove('hidden');
}

function cerrarModalOferta() {
    document.getElementById('modal-oferta-compra').classList.add('hidden');
    jugadorAccionId = null;
}

async function confirmarOfertaCompra() {
    const raw      = document.getElementById('input-oferta').value.replace(/\./g, '').replace(/,/g, '');
    const cantidad = parseInt(raw);
    if (!cantidad || cantidad <= 0) {
        mostrarErrorElement('error-oferta', 'Introduce una cantidad válida.');
        return;
    }
    try {
        // Reutilizamos el endpoint de puja del mercado
        const instanciaRes = await fetch(`/mercado/liga/${ligaId}`, { credentials: 'include' });
        if (!instanciaRes.ok) { mostrarErrorElement('error-oferta', 'Error al obtener el mercado.'); return; }
        const instancia = await instanciaRes.json();

        const res = await fetch(
            `/mercado/pujar?jugadorFantasyId=${jugadorAccionId}&instanciaId=${instancia.id}&cantidad=${cantidad}`,
            { method: 'POST', credentials: 'include' }
        );
        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorElement('error-oferta', texto || 'Error al enviar la oferta.');
            return;
        }
        cerrarModalOferta();
        mostrarToast('✓ Oferta enviada');
    } catch (e) {
        mostrarErrorElement('error-oferta', 'Error de conexión.');
    }
}

// ─── Pagar cláusula ───────────────────────────────────────────────────────────
function abrirPagarClausula(jfId, nombre, clausula) {
    jugadorClausulaId = jfId;
    document.getElementById('clausula-otro-nombre').textContent = nombre;
    document.getElementById('clausula-otro-valor').textContent  = formatearDinero(clausula);
    document.getElementById('clausula-otro-saldo').textContent  = formatearDinero(miDinero);
    document.getElementById('error-clausula-otro').classList.add('hidden');
    document.querySelectorAll('.acciones-dropdown').forEach(d => d.classList.add('hidden'));
    document.getElementById('modal-clausula-otro').classList.remove('hidden');
}

function cerrarModalClausulaOtro() {
    document.getElementById('modal-clausula-otro').classList.add('hidden');
    jugadorClausulaId = null;
}

async function confirmarPagarClausula() {
    if (!jugadorClausulaId || !miEquipoId) return;
    try {
        const res = await fetch(
            `/jugadores-fantasy/${jugadorClausulaId}/clausulazo?equipoCompradorId=${miEquipoId}`,
            { method: 'POST', credentials: 'include' }
        );
        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorElement('error-clausula-otro', texto || 'Error al pagar la cláusula.');
            return;
        }
        cerrarModalClausulaOtro();
        mostrarToast('✓ Cláusula pagada, jugador fichado');
        await cargarPlantillaOtro();
        renderizarPlantillaOtro();
    } catch (e) {
        mostrarErrorElement('error-clausula-otro', 'Error de conexión.');
    }
}

// ─── Tab puntos ───────────────────────────────────────────────────────────────
async function renderizarPuntosOtro() {
    document.getElementById('jornada-label').textContent = `Jornada ${jornadaActual}`;
    const campo = document.getElementById('campo-puntos');
    campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Cargando...</div>';

    try {
        const res = await fetch(
            `/equipos-fantasy/${equipoOtroId}/puntos/jornada/${jornadaActual}/info`,
            { credentials: 'include' }
        );
        if (!res.ok) {
            campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Sin datos</div>';
            return;
        }
        const datos           = await res.json();
        const formacionJornada = datos.formacion || '2-3-1';
        if (!datos.jugadores || datos.jugadores.length === 0) {
            campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Sin alineación para esta jornada</div>';
            return;
        }
        renderizarCampoPuntos(campo, datos.jugadores, formacionJornada);
    } catch (e) {
        campo.innerHTML = '<div style="color:rgba(255,255,255,0.7);text-align:center;padding:2rem">Error</div>';
    }
}

function cambiarJornada(delta) {
    const nueva = jornadaActual + delta;
    if (nueva < 1) return;
    jornadaActual = nueva;
    renderizarPuntosOtro();
}

function renderizarCampoPuntos(campo, datos, formacion) {
    campo.innerHTML = '';
    const lineas   = formacion.split('-').map(Number);
    const portero  = datos.find(j => j.posicion === 'PORTERO');
    const defensas = datos.filter(j => j.posicion === 'DEFENSA');
    const medios   = datos.filter(j => j.posicion === 'MEDIOCENTRO');
    const delant   = datos.filter(j => j.posicion === 'DELANTERO');

    const lPortero = document.createElement('div');
    lPortero.className = 'linea-campo';
    if (portero) lPortero.appendChild(crearCartaPuntos(portero));
    campo.appendChild(lPortero);

    [defensas, medios, delant].forEach((grupo, idx) => {
        const linea = document.createElement('div');
        linea.className = 'linea-campo';
        grupo.slice(0, lineas[idx] ?? 0).forEach(j => linea.appendChild(crearCartaPuntos(j)));
        campo.appendChild(linea);
    });
}

function crearCartaPuntos(jugador) {
    const carta = document.createElement('div');
    carta.className = 'carta-jugador';
    carta.style.cursor = 'pointer';
    carta.onclick = () => {
        const jf = plantillaOtro.find(j => j.nombre === jugador.nombre);
        if (jf) abrirDetalleJugador(jf.id);
    };
    carta.innerHTML = `
        <div class="carta-foto"><span>${ICONOS_POSICION[jugador.posicion] ?? '👤'}</span></div>
        <div class="carta-nombre">${jugador.nombre.split(' ').pop()}</div>
        <div class="carta-puntos-jornada">${jugador.jugo ? jugador.puntos + ' pts' : '—'}</div>
    `;
    return carta;
}

// ─── Detalle jugador (igual que en plantilla.js) ──────────────────────────────
async function cargarJornadasJugador(jugadorFantasyId) {
    try {
        const res = await fetch(`/equipos-fantasy/jugador/${jugadorFantasyId}/jornadas`, {
            credentials: 'include'
        });
        if (!res.ok) return [];
        return await res.json();
    } catch (e) { return []; }
}

async function abrirDetalleJugador(jugadorFantasyId) {
    const jugador  = plantillaOtro.find(j => j.id === jugadorFantasyId);
    if (!jugador) return;
    const jornadas = await cargarJornadasJugador(jugadorFantasyId);

    document.getElementById('detalle-icono').textContent          = ICONOS_POSICION[jugador.posicion] ?? '👤';
    document.getElementById('detalle-nombre').textContent         = jugador.nombre;
    document.getElementById('detalle-equipo').textContent         = jugador.nombreEquipoReal || 'Sin equipo';
    document.getElementById('detalle-posicion-badge').textContent = jugador.posicion.charAt(0) + jugador.posicion.slice(1).toLowerCase();
    document.getElementById('detalle-posicion-badge').className   = `badge-posicion ${jugador.posicion}`;
    document.getElementById('detalle-puntos').textContent         = jugador.puntosTotal;
    document.getElementById('detalle-media').textContent          = jugador.mediaPuntos.toFixed(2);
    document.getElementById('detalle-valor').textContent          = formatearDinero(jugador.valorMercado);

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
    const jorn     = jornadas[window._jornadaDetalleActual];

    document.getElementById('detalle-jornada-label').textContent  = `Jornada ${jorn.jornada}`;
    document.getElementById('detalle-jornada-puntos').textContent = jorn.jugo ? jorn.puntos : '—';

    const stats     = document.getElementById('detalle-stats-tabla');
    const esPortero = jugador?.posicion === 'PORTERO';

    if (!jorn.jugo) { stats.innerHTML = '<div class="sin-datos">No jugó</div>'; return; }

    stats.innerHTML = `
        <div class="stat-fila"><span class="stat-cantidad">${jorn.goles}</span><span class="stat-nombre">Goles</span><span class="stat-puntos ${jorn.goles > 0 ? 'positivo' : 'neutro'}">${jorn.goles * 4}</span></div>
        <div class="stat-fila"><span class="stat-cantidad">${jorn.asistencias}</span><span class="stat-nombre">Asistencias</span><span class="stat-puntos ${jorn.asistencias > 0 ? 'positivo' : 'neutro'}">${jorn.asistencias * 2}</span></div>
        <div class="stat-fila"><span class="stat-cantidad">${jorn.tarjetasAmarillas}</span><span class="stat-nombre">T. Amarillas</span><span class="stat-puntos ${jorn.tarjetasAmarillas > 0 ? 'negativo' : 'neutro'}">${jorn.tarjetasAmarillas * -1}</span></div>
        <div class="stat-fila"><span class="stat-cantidad">${jorn.tarjetasRojas}</span><span class="stat-nombre">T. Rojas</span><span class="stat-puntos ${jorn.tarjetasRojas > 0 ? 'negativo' : 'neutro'}">${jorn.tarjetasRojas * -3}</span></div>
        ${esPortero ? `<div class="stat-fila"><span class="stat-cantidad">${jorn.paradas}</span><span class="stat-nombre">Paradas</span><span class="stat-puntos ${jorn.paradas > 0 ? 'positivo' : 'neutro'}">${jorn.paradas}</span></div>` : ''}
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

// ─── Utilidades ───────────────────────────────────────────────────────────────
function formatearDinero(cantidad) {
    if (cantidad >= 1000000) return (cantidad / 1000000).toFixed(1) + 'M';
    if (cantidad >= 1000)    return (cantidad / 1000).toFixed(0) + 'K';
    return cantidad.toString();
}

function formatearInputDinero(input) {
    let valor = input.value.replace(/\./g, '').replace(/[^0-9]/g, '');
    if (valor === '') { input.value = ''; return; }
    input.value = parseInt(valor).toLocaleString('es-ES');
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