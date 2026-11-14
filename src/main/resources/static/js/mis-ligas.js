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
        // Una sola petición ligera con todas las ligas y su resumen (posición, puntos, dinero, aviso)
        const res = await fetch('/equipos-fantasy/usuario/mis-ligas-resumen', {
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

async function renderizarLigas(ligas) {
    const contenedor = document.getElementById('lista-ligas');
    contenedor.innerHTML = '';

    // El resumen ya viene incluido en cada liga (endpoint /equipos-fantasy/usuario/mis-ligas-resumen)
    const datosLigas = ligas.map(liga => ({
        liga,
        equipoId: liga.equipoId,
        posicion: liga.posicion != null ? liga.posicion + 'º' : '—',
        puntos:   liga.puntos != null ? liga.puntos : '—',
        dinero:   liga.dinero != null ? formatearDinero(liga.dinero) : '—',
        tieneAviso: !!liga.tieneAviso
    }));

    for (const { liga, equipoId, posicion, puntos, dinero, tieneAviso } of datosLigas) {
        const card = document.createElement('div');
        card.className = 'liga-card';
        card.innerHTML = `
            <img class="liga-card-escudo" src="/images/escudoETSIINF.png" alt="ETSIINF" onclick="entrarALiga(${liga.id})">
            <div class="liga-card-info" onclick="entrarALiga(${liga.id})">
                <div class="liga-card-nombre">${liga.name}</div>
                <div class="liga-card-stats">
                    <div class="liga-stat">
                        <span class="liga-stat-label">Posición</span>
                        <span class="liga-stat-valor">${posicion}</span>
                    </div>
                    <div class="liga-stat">
                        <span class="liga-stat-label">Puntos</span>
                        <span class="liga-stat-valor">${puntos}</span>
                    </div>
                    <div class="liga-stat">
                        <span class="liga-stat-label">Dinero</span>
                        <span class="liga-stat-valor">${dinero}</span>
                    </div>
                </div>
            </div>
            <div class="liga-card-menu">
                ${tieneAviso ? `<span class="aviso-alineacion" title="Alineación incompleta o saldo negativo">
                    <svg viewBox="0 0 24 24" width="22" height="22" aria-label="Aviso">
                        <path d="M12 2.5 L23 21.5 H1 Z" fill="#e53935" stroke="#e53935" stroke-width="1.5" stroke-linejoin="round"/>
                        <rect x="11" y="9" width="2" height="6.5" rx="1" fill="#fff"/>
                        <circle cx="12" cy="18.2" r="1.2" fill="#fff"/>
                    </svg>
                </span>` : ''}
                <button class="btn-tres-puntos" onclick="toggleDropdown(event, ${liga.id}, '${escapar(liga.code)}')">⋯</button>
                <div id="dropdown-${liga.id}" class="dropdown-menu hidden">
                    <div class="dropdown-item" onclick="mostrarModalInvitar('${escapar(liga.code)}')">
                        Invitar amigo
                    </div>
                    <div class="dropdown-item danger" onclick="confirmarAbandonar(${liga.id}, '${escapar(liga.name)}')">
                        Abandonar liga
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
function toggleDropdown(event, ligaId, codigo) {
    event.stopPropagation();
    document.querySelectorAll('.dropdown-menu').forEach(d => {
        if (d.id !== `dropdown-${ligaId}`) d.classList.add('hidden');
    });
    const dropdown = document.getElementById(`dropdown-${ligaId}`);
    dropdown.classList.toggle('hidden');

    // Comprobar si se sale de la pantalla y abrir hacia arriba si es necesario
    if (!dropdown.classList.contains('hidden')) {
        const rect = dropdown.getBoundingClientRect();
        if (rect.bottom > window.innerHeight) {
            dropdown.classList.add('abre-arriba');
        } else {
            dropdown.classList.remove('abre-arriba');
        }
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

        const btn = document.querySelector('#modal-crear .btn-primary');
        if (btn.disabled) return;
        btn.disabled = true;
        btn.textContent = 'Creando...';

        mostrarCargando('Creando liga...');

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
        finally {
                btn.disabled = false;
                btn.textContent = 'Crear liga';
                ocultarCargando();
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

    const btn = document.querySelector('#modal-unirse .btn-primary');
    if (btn.disabled) return;
    btn.disabled = true;
    btn.textContent = 'Uniéndose...';

    mostrarCargando('Uniéndose a la liga...');

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
    } finally {
        btn.disabled = false;
        btn.textContent = 'Unirse';
        ocultarCargando();
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