let ligaId        = null;
let equipoId      = null;
let instanciaId   = null;
let finMercado    = null;
let misDatos      = null;
let pujaJugadorId = null;
let contadorInterval = null;

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
        const res = await fetch(`/mercado/liga/${ligaId}`, { credentials: 'include' });
        if (!res.ok) {
            document.getElementById('lista-mercado').innerHTML =
                '<div class="cargando-ligas">No hay mercado activo en esta liga.</div>';
            return;
        }

        const instancia = await res.json();
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
    document.getElementById('saldo-disponible').textContent = formatearDinero(saldo);

    // Calcular total de pujas activas
    try {
        const res = await fetch(`/mercado/mis-pujas?ligaId=${ligaId}`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        const pujas = await res.json();
        const totalPujas = pujas.reduce((sum, p) => sum + p.cantidad, 0);

        if (totalPujas > 0) {
            document.getElementById('saldo-tras-pujas').textContent =
                formatearDinero(saldo - totalPujas);
            document.getElementById('saldo-tras-pujas-container').classList.remove('hidden');
        }
    } catch (e) {}
}

function renderizarMercado(jugadores) {
    const contenedor = document.getElementById('lista-mercado');

    if (!jugadores || jugadores.length === 0) {
        contenedor.innerHTML = '<div class="cargando-ligas">No hay jugadores disponibles.</div>';
        return;
    }

    contenedor.innerHTML = jugadores.map(j => {
        const enVenta = j.enVenta && j.nombreEquipoFantasyDueno;
        return `
        <div class="jugador-card-mercado">
            <div class="jugador-foto-grande">
                <span>${ICONOS_POSICION[j.posicion] ?? '👤'}</span>
            </div>
            <div class="jugador-card-info" style="flex:1">
                <div class="jugador-card-nombre">${j.nombre}</div>
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
                ${!enVenta ? `<div class="temporizador-jugador">⏱ --:--:--</div>` : ''}
            </div>
            <button class="btn-fichar" onclick="abrirModalPuja(${j.id}, '${escapar(j.nombre)}', ${j.valorMercado})">
                Fichar
            </button>
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
        const res = await fetch(
            `/mercado/pujar?jugadorFantasyId=${pujaJugadorId}&instanciaId=${instanciaId}&cantidad=${cantidad}`,
            { method: 'POST', credentials: 'include' }
        );

        if (!res.ok) {
            const texto = await res.text();
            mostrarErrorElement('error-puja', texto || 'Error al realizar la puja.');
            return;
        }

        cerrarModalPuja();
        mostrarToast('✓ Puja realizada');
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
        if (!res.ok) { contenedor.innerHTML = '<div class="cargando-ligas">Error al cargar.</div>'; return; }

        const ventas = await res.json();
        if (ventas.length === 0) {
            contenedor.innerHTML = '<div class="cargando-ligas">No tienes jugadores en venta.</div>';
            return;
        }

        contenedor.innerHTML = ventas.map(v => `
            <div class="venta-card">
                <div class="jugador-foto-grande">
                    <span>${ICONOS_POSICION[v.posicion] ?? '👤'}</span>
                </div>
                <div class="puja-info">
                    <div class="puja-nombre">${v.jugadorNombre}</div>
                    <div class="puja-detalle">
                        ${v.equipoReal || 'Sin equipo'} ·
                        ${v.posicion.charAt(0) + v.posicion.slice(1).toLowerCase()}
                    </div>
                    <div class="puja-detalle">Valor: ${formatearDinero(v.valorMercado)}</div>
                    <div class="puja-detalle" style="color:#27ae60;font-weight:700">
                        Oferta: ${formatearDinero(v.cantidad)}
                    </div>
                </div>
                <button class="btn-aceptar-oferta" onclick="aceptarOferta(${v.id})">
                    Aceptar
                </button>
            </div>
        `).join('');
    } catch (e) {
        contenedor.innerHTML = '<div class="cargando-ligas">Error al cargar.</div>';
    }
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