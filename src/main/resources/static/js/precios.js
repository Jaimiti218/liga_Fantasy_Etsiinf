const ICONOS_POSICION = {
    'PORTERO': '🧤', 'DEFENSA': '🧱',
    'MEDIOCENTRO': '⚙️', 'DELANTERO': '🎯'
};

let tabActual     = 'subidas';
let periodoActual = 'semana';

document.addEventListener('DOMContentLoaded', () => {
    cargarDatos();
});

function cambiarTab(tab) {
    tabActual = tab;
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(`tab-${tab}`).classList.add('active');
    cargarDatos();
}

function cambiarPeriodo(periodo) {
    periodoActual = periodo;
    document.querySelectorAll('.btn-periodo').forEach(b => b.classList.remove('active'));
    document.getElementById(`periodo-${periodo}`).classList.add('active');
    cargarDatos();
}

async function cargarDatos() {
    const contenedor = document.getElementById('lista-precios');
    contenedor.innerHTML = '<div class="cargando-ligas">Cargando...</div>';

    try {
        const res = await fetch(`/precios/${tabActual}?periodo=${periodoActual}`,
            { credentials: 'include' });
        if (!res.ok) {
            contenedor.innerHTML = '<div class="sin-datos-precio">Error al cargar.</div>';
            return;
        }

        const datos = await res.json();

        if (datos.length === 0) {
            contenedor.innerHTML = `
                <div class="sin-datos-precio">
                    <div style="font-size:2rem;margin-bottom:0.5rem">📊</div>
                    Sin datos para este periodo todavía.<br>
                    <small>Los precios se actualizan cada día a las 23:30h.</small>
                </div>`;
            return;
        }

        contenedor.innerHTML = datos.map((j, i) => {
            const esSube     = j.diferencia > 0;
            const signo      = esSube ? '+' : '';
            const claseBadge = esSube ? 'variacion-sube' : 'variacion-baja';
            const flecha     = esSube ? '↑' : '↓';
            const posLabel   = j.posicion
                ? j.posicion.charAt(0) + j.posicion.slice(1).toLowerCase()
                : '—';

            return `
            <div class="precio-card">
                <span class="precio-pos">${i + 1}</span>
                <div class="precio-icono">
                    <span>${ICONOS_POSICION[j.posicion] ?? '👤'}</span>
                </div>
                <div class="precio-info">
                    <div class="precio-nombre">${j.nombre}</div>
                    <div class="precio-meta">
                        <span class="badge-equipo">${j.equipo || 'Sin equipo'}</span>
                        <span class="badge-posicion ${j.posicion}"
                              style="font-size:0.65rem">${posLabel}</span>
                    </div>
                </div>
                <div class="precio-valores">
                    <span class="precio-anterior">${formatearDinero(j.precioAnterior)}</span>
                    <span class="precio-actual">${formatearDinero(j.precioActual)}</span>
                    <span class="variacion-badge ${claseBadge}">
                        ${flecha} ${signo}${formatearDinero(Math.abs(j.diferencia))}
                    </span>
                </div>
            </div>`;
        }).join('');

    } catch (e) {
        contenedor.innerHTML = '<div class="sin-datos-precio">Error al cargar los datos.</div>';
    }
}

function formatearDinero(cantidad) {
    if (cantidad >= 1000000) return (cantidad / 1000000).toFixed(1) + 'M';
    if (cantidad >= 1000)    return (cantidad / 1000).toFixed(0) + 'K';
    return cantidad.toString();
}