let ligaId        = null;
let equipoId      = null;
let instanciaId   = null;
let finMercado    = null;
let misDatos      = null;
let pujaJugadorId = null;
let contadorInterval = null;
let misPujasActivas = []; // {jugadorFantasyId, pujaId, cantidad}
let pujaEditandoId = null;
let contadoresPujas = {};


const ICONOS_POSICION = {
    'PORTERO': '🧤', 'DEFENSA': '🛡',
    'MEDIOCENTRO': '⚙️', 'DELANTERO': '⚡'
};

document.addEventListener('DOMContentLoaded', async () => {
    const partes = window.location.pathname.split('/');
    ligaId = parseInt(partes[partes.length - 1]);
    if (!ligaId) { window.location.href = '/fantasy/mis-ligas'; return; }

    await cargarDatosEquipo();
    await cargarMercado();
    cambiarTab('mercado');
});

function irAClasificacion() { window.location.href = `/fantasy/liga/${ligaId}`; }
function irAPlantilla()      { window.location.href = `/fantasy/plantilla/${equipoId}?liga=${ligaId}`; }

function toggleAccionesPuja(event, jugadorId) {
    event.stopPropagation();
    const menu = document.getElementById(`puja-menu-${jugadorId}`);
    const abierto = !menu.classList.contains('hidden');
    // Cerrar todos
    document.querySelectorAll('.acciones-dropdown').forEach(d => d.classList.add('hidden'));
    if (!abierto) menu.classList.remove('hidden');
}

function editarPuja(jugadorId, nombre, valorMercado, pujaId) {
    pujaJugadorId = jugadorId;
    pujaEditandoId = pujaId;
    const pujaActual = misPujasActivas.find(p => p.id === pujaId);
    document.getElementById('puja-jugador-nombre').textContent = nombre;
    document.getElementById('puja-saldo').textContent = formatearDineroCompleto(misDatos?.dinero ?? 0);
    document.getElementById('puja-valor-mercado').textContent = formatearDineroCompleto(valorMercado);
    document.getElementById('input-puja').value = pujaActual ? pujaActual.cantidad.toLocaleString('es-ES') : '';
    document.getElementById('error-puja').classList.add('hidden');
    document.getElementById('modal-puja').classList.remove('hidden');
    document.querySelectorAll('.acciones-dropdown').forEach(d => d.classList.add('hidden'));
}

async function eliminarPuja(pujaId, jugadorId) {
    if (!confirm('¿Seguro que quieres eliminar esta puja?')) return;
    try {
        const res = await fetch(`/mercado/puja/${pujaId}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        if (!res.ok) { alert('Error al eliminar la puja.'); return; }
        mostrarToast('✓ Puja eliminada');
        await cargarMisPujas();
        await cargarMercado();
        await actualizarSaldo();
    } catch (e) {
        alert('Error de conexión.');
    }
}

// ─── Cargar equipo del usuario ────────────────────────────────────────────────
async function cargarDatosEquipo() {
    try {
        const res = await fetch(`/equipos-fantasy/liga/${ligaId}/mi-equipo`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        misDatos = await res.json();
        equipoId = misDatos.id;
    } catch (e) {}
}

async function cargarMisPujas() {
    try {
        const res = await fetch(`/mercado/mis-pujas?ligaId=${ligaId}`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        misPujasActivas = await res.json();
    } catch (e) {
        misPujasActivas = [];
    }
}

// ─── Tabs principales ─────────────────────────────────────────────────────────
function cambiarTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-seccion').forEach(s => s.classList.add('hidden'));
    document.getElementById(`tab-${tab}`).classList.add('active');
    document.getElementById(`seccion-${tab}`).classList.remove('hidden');

    if (tab === 'mercado')     cargarMercado();
    if (tab === 'operaciones') { cargarCompras(); cambiarOpTab('compras'); }
}

// ─── Tabs operaciones ─────────────────────────────────────────────────────────
function cambiarOpTab(sub) {
    document.querySelectorAll('.op-tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.op-seccion').forEach(s => s.classList.add('hidden'));
    document.getElementById(`op-${sub}`).classList.add('active');
    document.getElementById(`seccion-${sub}`).classList.remove('hidden');

    if (sub === 'compras') cargarCompras();
    if (sub === 'ventas')  cargarVentas();
}

// ─── Cargar mercado ───────────────────────────────────────────────────────────
async function cargarMercado() {
    try {
        await cargarMisPujas();
        const [resMercado, resContadores] = await Promise.all([
            fetch(`/mercado/liga/${ligaId}`, { credentials: 'include' }),
            fetch(`/mercado/liga/${ligaId}/contadores-pujas`, { credentials: 'include' })
        ]);
        if (!resMercado.ok) { /* error */ return; }
        contadoresPujas = resContadores.ok ? await resContadores.json() : {};
        const instancia = await resMercado.json();
        instanciaId = instancia.id;
        finMercado  = new Date(instancia.fin);
        iniciarContador();
        actualizarSaldo();
        renderizarMercado(instancia.jugadores);

    } catch (e) {
        document.getElementById('lista-mercado').innerHTML =
            '<div class="cargando-ligas">Error al cargar el mercado.</div>';
    }
}

function iniciarContador() {
    if (contadorInterval) clearInterval(contadorInterval);
    contadorInterval = setInterval(() => {
        const ahora = new Date();
        const diff  = finMercado - ahora;

        if (diff <= 0) {
            document.getElementById('contador-mercado').textContent = '00:00:00';
            clearInterval(contadorInterval);
            return;
        }

        const horas   = Math.floor(diff / 3600000);
        const minutos = Math.floor((diff % 3600000) / 60000);
        const segundos = Math.floor((diff % 60000) / 1000);

        document.getElementById('contador-mercado').textContent =
            `${pad(horas)}:${pad(minutos)}:${pad(segundos)}`;

        // Actualizar también los temporizadores de cada jugador
        document.querySelectorAll('.temporizador-jugador').forEach(el => {
            el.textContent = `⏱ ${pad(horas)}:${pad(minutos)}:${pad(segundos)}`;
        });
    }, 1000);
}

function pad(n) { return n.toString().padStart(2, '0'); }

async function actualizarSaldo() {
    if (!misDatos) return;

    const saldo = misDatos.dinero;
    const elSaldo = document.getElementById('saldo-disponible');
    elSaldo.textContent = formatearDineroCompleto(saldo);
    elSaldo.style.color = saldo < 0 ? '#cb0606' : 'white';

    try {
        const res = await fetch(`/mercado/mis-pujas?ligaId=${ligaId}`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        const pujas      = await res.json();
        const totalPujas = pujas.reduce((sum, p) => sum + p.cantidad, 0);

        if (totalPujas > 0) {
            const saldoFinal = saldo - totalPujas;
            document.getElementById('saldo-label-pujas').textContent =
                `Pujas activas: -${formatearDineroCompleto(totalPujas)}`;
            const elFinal = document.getElementById('saldo-final-pujas');
            elFinal.textContent = formatearDineroCompleto(saldoFinal);
            elFinal.style.color = saldoFinal < 0 ? '#ac0101' : 'white';
            document.getElementById('saldo-tras-pujas-container').classList.remove('hidden');
        }
    } catch (e) {}
}

async function renderizarMercado(jugadores) {
    const contenedor = document.getElementById('lista-mercado');

    if (!jugadores || jugadores.length === 0) {
        contenedor.innerHTML = '<div class="cargando-ligas">No hay jugadores disponibles.</div>';
        return;
    }

    const jornadasMap = {};
    await Promise.all(jugadores.map(async j => {
        jornadasMap[j.id] = await cargarJornadasJugador(j.id);
    }));

    contenedor.innerHTML = jugadores.map(j => {
        const enVenta      = j.enVenta && j.nombreEquipoFantasyDueno;
        const esMioEnVenta = enVenta && j.userIdDueno !== undefined && j.userIdDueno === misDatos?.userId;
        const pujaActiva   = misPujasActivas.find(p => p.jugadorFantasyId === j.id);
        const jornadas     = jornadasMap[j.id] ?? [];
        const ultimas5     = jornadas.slice(-5);
        const numPujas = contadoresPujas[j.id] ?? 0;
        while (ultimas5.length < 5) ultimas5.unshift(null);

        const jornadasHtml = ultimas5.map(jorn => {
            if (!jorn) return `<div class="jornada-mini jornada-vacia">—</div>`;
            const color = jorn.puntos >= 8 ? 'alta' : jorn.puntos >= 4 ? 'media' : 'baja';
            return `<div class="jornada-mini jornada-${color}">
                J${jorn.jornada}<br><strong>${jorn.jugo ? jorn.puntos : '—'}</strong>
            </div>`;
        }).join('');

        let botonAccion;
        if (esMioEnVenta) {
            // El dueño no ve botón de fichar
            botonAccion = '';
        } else if (pujaActiva) {
            botonAccion = `
                <div style="position:relative">
                    <button class="btn-acciones-puja" onclick="toggleAccionesPuja(event, ${j.id})">
                        Acciones ▾
                    </button>
                    <div id="puja-menu-${j.id}" class="acciones-dropdown hidden">
                        <div class="acciones-item" onclick="editarPuja(${j.id}, '${escapar(j.nombre)}', ${j.valorMercado}, ${pujaActiva.id})">
                            ✏️ Editar puja
                        </div>
                        <div class="acciones-item" style="color:#c0392b" onclick="eliminarPuja(${pujaActiva.id}, ${j.id})">
                            🗑️ Eliminar puja
                        </div>
                    </div>
                </div>`;
        } else {
            botonAccion = `
                <button class="btn-fichar" onclick="abrirModalPuja(${j.id}, '${escapar(j.nombre)}', ${j.valorMercado})">
                    Fichar
                </button>`;
        }

        return `
        <div class="jugador-card-mercado" onclick="abrirDetalleJugadorMercado(${j.id})" style="cursor:pointer">
            <div class="jugador-foto-grande">
                <span>${ICONOS_POSICION[j.posicion] ?? '👤'}</span>
            </div>
            <div class="jugador-card-info" style="flex:1">
                <div style="display:flex;align-items:center;justify-content:space-between;gap:0.5rem">
                    <div class="jugador-card-nombre">${j.nombre}</div>
                    ${numPujas > 0 ? `<span class="contador-pujas">${numPujas} puja${numPujas > 1 ? 's' : ''}</span>` : ''}
                </div>
                <div class="jugador-card-equipo">
                    <span class="badge-equipo">${j.nombreEquipoReal || 'Sin equipo'}</span>
                    <span class="badge-posicion ${j.posicion}">
                        ${j.posicion.charAt(0) + j.posicion.slice(1).toLowerCase()}
                    </span>
                    ${enVenta ? `<span class="badge-en-venta">🏷 ${j.nombreEquipoFantasyDueno}</span>` : ''}
                </div>
                <div class="jugador-card-stats" style="margin-top:0.4rem">
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
                </div>
                <div class="jornadas-recientes">${jornadasHtml}</div>
                ${!enVenta ? `<div class="temporizador-jugador">⏱ --:--:--</div>` : ''}
                ${pujaActiva ? `<div class="puja-actual-badge">Tu puja: ${formatearDinero(pujaActiva.cantidad)}</div>` : ''}
            </div>
            <div onclick="event.stopPropagation()">
                ${botonAccion}
            </div>
        </div>`;
    }).join('');
}

// ─── Modal puja ───────────────────────────────────────────────────────────────
function abrirModalPuja(jugadorId, nombre, valorMercado) {
    pujaJugadorId = jugadorId;
    document.getElementById('puja-jugador-nombre').textContent = nombre;
    document.getElementById('puja-saldo').textContent          = formatearDinero(misDatos?.dinero ?? 0);
    document.getElementById('puja-valor-mercado').textContent  = formatearDinero(valorMercado);
    document.getElementById('input-puja').value                = '';
    document.getElementById('error-puja').classList.add('hidden');
    document.getElementById('modal-puja').classList.remove('hidden');
}

function cerrarModalPuja() {
    document.getElementById('modal-puja').classList.add('hidden');
    pujaJugadorId = null;
}

async function confirmarPuja() {
    const raw      = document.getElementById('input-puja').value.replace(/\./g, '').replace(/,/g, '');
    const cantidad = parseInt(raw);

    if (!cantidad || cantidad <= 0) {
        mostrarErrorElement('error-puja', 'Introduce una cantidad válida.');
        return;
    }

    try {
        let url, method;
        if (pujaEditandoId) {
            url    = `/mercado/puja/${pujaEditandoId}?cantidad=${cantidad}`;
            method = 'PUT';
        } else {
            url    = `/mercado/pujar?jugadorFantasyId=${pujaJugadorId}&instanciaId=${instanciaId}&cantidad=${cantidad}`;
            method = 'POST';
        }

        const res = await fetch(url, { method, credentials: 'include' });

        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorElement('error-puja', texto || 'Error al realizar la puja.');
            return;
        }

        pujaEditandoId = null;
        cerrarModalPuja();
        mostrarToast('✓ Puja guardada');
        await cargarMisPujas();
        await cargarMercado();
        await actualizarSaldo();

    } catch (e) {
        mostrarErrorElement('error-puja', 'Error de conexión.');
    }
}

// ─── Cargar compras ───────────────────────────────────────────────────────────
async function cargarCompras() {
    const contenedor = document.getElementById('lista-compras');
    try {
        const res = await fetch(`/mercado/mis-pujas?ligaId=${ligaId}`, {
            credentials: 'include'
        });
        if (!res.ok) { contenedor.innerHTML = '<div class="cargando-ligas">Error al cargar.</div>'; return; }

        const pujas = await res.json();
        if (pujas.length === 0) {
            contenedor.innerHTML = '<div class="cargando-ligas">No tienes pujas activas.</div>';
            return;
        }

        contenedor.innerHTML = pujas.map(p => `
            <div class="puja-card">
                <div class="jugador-foto-grande">
                    <span>${ICONOS_POSICION[p.posicion] ?? '👤'}</span>
                </div>
                <div class="puja-info">
                    <div class="puja-nombre">${p.jugadorNombre}</div>
                    <div class="puja-detalle">
                        ${p.equipoReal || 'Sin equipo'} ·
                        ${p.posicion.charAt(0) + p.posicion.slice(1).toLowerCase()}
                    </div>
                    <div class="puja-detalle">Valor: ${formatearDinero(p.valorMercado)}</div>
                    ${p.duenoNombre ? `<div class="puja-detalle" style="color:#0f3460;font-weight:600">🏷 Propietario: ${p.duenoNombre}</div>` : ''}
                </div>
                <div class="puja-cantidad">${formatearDinero(p.cantidad)}</div>
            </div>
        `).join('');
    } catch (e) {
        contenedor.innerHTML = '<div class="cargando-ligas">Error al cargar.</div>';
    }
}

// ─── Cargar ventas ────────────────────────────────────────────────────────────
async function cargarVentas() {
    const contenedor = document.getElementById('lista-ventas');
    try {
        const res = await fetch(`/mercado/mis-ventas?ligaId=${ligaId}`, {
            credentials: 'include'
        });
        if (!res.ok) { contenedor.innerHTML = '<div class="cargando-ligas">Error.</div>'; return; }

        const jugadores = await res.json();
        if (jugadores.length === 0) {
            contenedor.innerHTML = '<div class="cargando-ligas">No tienes jugadores en venta.</div>';
            return;
        }

        contenedor.innerHTML = jugadores.map(j => `
            <div class="venta-card">
                <div class="jugador-foto-grande">
                    <span>${ICONOS_POSICION[j.posicion] ?? '👤'}</span>
                </div>
                <div class="puja-info">
                    <div class="puja-nombre">${j.nombre}</div>
                    <div class="puja-detalle">
                        ${j.nombreEquipoReal || 'Sin equipo'} ·
                        ${j.posicion.charAt(0) + j.posicion.slice(1).toLowerCase()}
                    </div>
                    <div class="puja-detalle">Valor: ${formatearDinero(j.valorMercado)}</div>
                    <div class="puja-detalle" style="color:#f39c12;font-weight:600">
                        ⏳ En espera de ofertas
                    </div>
                </div>
                <button class="btn-quitar-mercado-ventas" 
                        onclick="quitarDelMercadoDesdeVentas(${j.id})">
                    Quitar
                </button>
            </div>
        `).join('');
    } catch (e) {
        contenedor.innerHTML = '<div class="cargando-ligas">Error al cargar.</div>';
    }
}

async function quitarDelMercadoDesdeVentas(jugadorFantasyId) {
    if (!confirm('¿Quieres retirar este jugador del mercado?')) return;
    try {
        const res = await fetch(`/mercado/vender/${jugadorFantasyId}`, {
            method: 'DELETE', credentials: 'include'
        });
        if (!res.ok) { alert('Error.'); return; }
        mostrarToast('✓ Jugador retirado del mercado');
        cargarVentas();
    } catch (e) { alert('Error de conexión.'); }
}

async function aceptarOferta(ofertaId) {
    if (!confirm('¿Seguro que quieres aceptar esta oferta?')) return;

    try {
        const res = await fetch(`/mercado/ventas/${ofertaId}/aceptar`, {
            method: 'POST', credentials: 'include'
        });
        if (!res.ok) { alert('Error al aceptar la oferta.'); return; }
        mostrarToast('✓ Jugador vendido');
        await cargarDatosEquipo();
        await cargarVentas();
        await actualizarSaldo();
    } catch (e) {
        alert('Error de conexión.');
    }
}

// ─── Formatear input dinero con puntos ────────────────────────────────────────
function formatearInputDinero(input) {
    let valor = input.value.replace(/\./g, '').replace(/[^0-9]/g, '');
    if (valor === '') { input.value = ''; return; }
    input.value = parseInt(valor).toLocaleString('es-ES');
}

// ─── Actualizar plantilla para mostrar boton "Añadir al mercado" ──────────────
// (Esta función se llama desde plantilla.js cuando el botón de acciones está activo)
async function ponerEnVenta(jugadorFantasyId) {
    try {
        const res = await fetch(`/mercado/vender/${jugadorFantasyId}`, {
            method: 'POST', credentials: 'include'
        });
        if (!res.ok) { alert('Error al poner en venta.'); return; }
        mostrarToast('✓ Jugador puesto en venta');
    } catch (e) {
        alert('Error de conexión.');
    }
}

// ─── Utilidades ───────────────────────────────────────────────────────────────
function formatearDinero(cantidad) {
    if (cantidad >= 1000000) return (cantidad / 1000000).toFixed(1) + 'M';
    if (cantidad >= 1000)    return (cantidad / 1000).toFixed(0) + 'K';
    return cantidad.toString();
}

function formatearDineroCompleto(cantidad) {
    return cantidad.toLocaleString('es-ES') + ' €';
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

async function cargarJornadasJugador(jugadorFantasyId) {
    try {
        const res = await fetch(`/equipos-fantasy/jugador/${jugadorFantasyId}/jornadas`, {
            credentials: 'include'
        });
        if (!res.ok) return [];
        return await res.json();
    } catch (e) { return []; }
}

async function abrirDetalleJugadorMercado(jugadorFantasyId) {
    // Buscar el jugador en la instancia actual
    const res = await fetch(`/mercado/liga/${ligaId}`, { credentials: 'include' });
    if (!res.ok) return;
    const instancia = await res.json();
    const jugador   = instancia.jugadores.find(j => j.id === jugadorFantasyId);
    if (!jugador) return;

    const jornadas = await cargarJornadasJugador(jugadorFantasyId);

    document.getElementById('detalle-icono').textContent          = ICONOS_POSICION[jugador.posicion] ?? '👤';
    document.getElementById('detalle-nombre').textContent         = jugador.nombre;
    document.getElementById('detalle-equipo').textContent         = jugador.nombreEquipoReal || 'Sin equipo';
    document.getElementById('detalle-posicion-badge').textContent = jugador.posicion.charAt(0) + jugador.posicion.slice(1).toLowerCase();
    document.getElementById('detalle-posicion-badge').className   = `badge-posicion ${jugador.posicion}`;
    document.getElementById('detalle-puntos').textContent         = jugador.puntosTotal;
    document.getElementById('detalle-media').textContent          = jugador.mediaPuntos.toFixed(2);
    document.getElementById('detalle-valor').textContent          = formatearDineroCompleto(jugador.valorMercado);

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

    const stats     = document.getElementById('detalle-stats-tabla');
    const esPortero = jugador?.posicion === 'PORTERO';

    if (!jorn.jugo) {
        stats.innerHTML = '<div class="sin-datos">No jugó en esta jornada</div>';
        return;
    }

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