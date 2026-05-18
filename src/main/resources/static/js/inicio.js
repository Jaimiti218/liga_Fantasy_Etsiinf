let usuarioActual = null;
let estadisticasData = [];

document.addEventListener('DOMContentLoaded', async () => {
    usuarioActual = await obtenerUsuarioActual();

    if (usuarioActual) {
        document.getElementById('saludo-usuario').style.display = 'flex';
        document.getElementById('saludo-usuario').style.alignItems = 'center';
        document.getElementById('saludo-usuario').style.gap = '0.4rem';
        document.getElementById('saludo-usuario').innerHTML =
            (usuarioActual.fotoPerfil
                ? `<img src="${usuarioActual.fotoPerfil}" style="width:28px;height:28px;border-radius:50%;object-fit:cover">`
                : `<span style="width:28px;height:28px;border-radius:50%;background:rgba(255,255,255,0.3);display:flex;align-items:center;justify-content:center">👤</span>`)
            + `<span> ${usuarioActual.username}</span>`;
        document.getElementById('saludo-usuario').classList.remove('hidden');
        document.getElementById('btn-sesion').textContent = 'Cerrar sesión';

        if (usuarioActual.role === 'ADMIN') {
            document.getElementById('btn-gestionar').classList.remove('hidden');
        }
    }
});

function manejarSesion() {
    if (usuarioActual) {
        cerrarSesionCompleta();
    } else {
        location.href = '/fantasy/auth?redirect=home';
    }
}

function irAlFantasy() {
    if (usuarioActual) {
        location.href = '/fantasy/mis-ligas';
    } else {
        location.href = '/fantasy/auth?redirect=fantasy';
    }
}

function abrirMenuPerfil() {
    const foto = usuarioActual?.fotoPerfil;
    const avatarEl = document.getElementById('avatar-perfil-grande');
    avatarEl.innerHTML = foto
        ? `<img src="${foto}" style="width:100%;height:100%;object-fit:cover">`
        : '👤';
    document.getElementById('perfil-nombre-display').textContent = usuarioActual?.username || '';
    document.getElementById('input-nuevo-username').value = usuarioActual?.username || '';
    document.getElementById('error-perfil').classList.add('hidden');
    if (document.getElementById('exito-perfil')) {
        document.getElementById('exito-perfil').classList.add('hidden');
    }
    document.getElementById('modal-perfil').classList.remove('hidden');
}

function cambiarFoto(input) {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async (e) => {
        const base64 = e.target.result;
        try {
            const res = await fetch('/users/foto-perfil', {
                method: 'PUT',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ foto: base64 })
            });
            if (!res.ok) {
                document.getElementById('error-perfil').textContent = 'Error al guardar la foto.';
                document.getElementById('error-perfil').classList.remove('hidden');
                return;
            }
            // Actualizar estado local
            usuarioActual.fotoPerfil = base64;
            // Actualizar vista previa en el modal
            document.getElementById('avatar-perfil-grande').innerHTML =
                `<img src="${base64}" style="width:100%;height:100%;object-fit:cover">`;
            // Actualizar avatar en la navbar
            actualizarNavbarAvatar();
            // Mostrar mensaje de éxito
            document.getElementById('exito-perfil').textContent = '✓ Foto actualizada';
            document.getElementById('exito-perfil').classList.remove('hidden');
            document.getElementById('error-perfil').classList.add('hidden');
        } catch (e) {
            document.getElementById('error-perfil').textContent = 'Error de conexión.';
            document.getElementById('error-perfil').classList.remove('hidden');
        }
    };
    reader.readAsDataURL(file);
}

function cerrarMenuPerfil() {
    document.getElementById('modal-perfil').classList.add('hidden');
}

function cambiarFoto(input) {
    console.log('cambiarFoto llamado', input.files);
    const file = input.files[0];
    if (!file) { console.log('no hay archivo'); return; }
    console.log('archivo:', file.name, file.size);
    const reader = new FileReader();
    reader.onload = async (e) => {
        const base64 = e.target.result;
        console.log('base64 longitud:', base64.length);
        try {
            const res = await fetch('/users/foto-perfil', {
                method: 'PUT',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ foto: base64 })
            });
            console.log('respuesta foto-perfil:', res.status);
            if (!res.ok) {
                document.getElementById('error-perfil').textContent = 'Error al guardar la foto.';
                document.getElementById('error-perfil').classList.remove('hidden');
                return;
            }
            usuarioActual.fotoPerfil = base64;
            document.getElementById('avatar-perfil-grande').innerHTML =
                `<img src="${base64}" style="width:100%;height:100%;object-fit:cover">`;
            actualizarNavbarAvatar();
            document.getElementById('exito-perfil').textContent = '✓ Foto actualizada';
            document.getElementById('exito-perfil').classList.remove('hidden');
        } catch(e) {
            console.error('error fetch foto:', e);
        }
    };
    reader.readAsDataURL(file);
}

async function guardarPerfil() {
    const nuevoUsername = document.getElementById('input-nuevo-username').value.trim();
    if (!nuevoUsername) {
        document.getElementById('error-perfil').textContent = 'El nombre no puede estar vacío.';
        document.getElementById('error-perfil').classList.remove('hidden');
        return;
    }
    try {
        const res = await fetch('/users/username', {
            method: 'PUT',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: nuevoUsername })
        });
        if (!res.ok) {
            const texto = await res.text();
            document.getElementById('error-perfil').textContent = texto || 'Error al guardar.';
            document.getElementById('error-perfil').classList.remove('hidden');
            return;
        }
        usuarioActual.username = nuevoUsername;
        document.getElementById('perfil-nombre-display').textContent = nuevoUsername;
        actualizarNavbarAvatar();
        document.getElementById('exito-perfil').textContent = '✓ Nombre actualizado';
        document.getElementById('exito-perfil').classList.remove('hidden');
        document.getElementById('error-perfil').classList.add('hidden');
    } catch (e) {
        document.getElementById('error-perfil').textContent = 'Error de conexión.';
        document.getElementById('error-perfil').classList.remove('hidden');
    }
}

function actualizarNavbarAvatar() {
    const saludo = document.getElementById('saludo-usuario');
    saludo.innerHTML =
        (usuarioActual.fotoPerfil
            ? `<img src="${usuarioActual.fotoPerfil}" style="width:28px;height:28px;border-radius:50%;object-fit:cover">`
            : `<span style="width:28px;height:28px;border-radius:50%;background:rgba(255,255,255,0.3);display:flex;align-items:center;justify-content:center">👤</span>`)
        + `<span> ${usuarioActual.username}</span>`;
}





function cambiarSeccion(seccion) {
    document.querySelectorAll('.inicio-tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.inicio-seccion').forEach(s => s.classList.add('hidden'));
    document.getElementById(`tab-${seccion}`).classList.add('active');
    document.getElementById(`seccion-${seccion}`).classList.remove('hidden');

    if (seccion === 'clasificacion') cargarClasificacion();
    if (seccion === 'partidos')      cargarPartidos();
    if (seccion === 'estadisticas')  cargarEstadisticas();
}

// ─── Clasificación ──────────────────────────────────────────────────────────
async function cargarClasificacion() {
    const wrap = document.getElementById('tabla-clasificacion-wrap');
    try {
        const res  = await fetch('/equipos/clasificacion');
        const data = await res.json();

        if (data.length === 0) {
            wrap.innerHTML = '<div class="cargando-inicio">Sin datos de clasificación.</div>';
            return;
        }

        wrap.innerHTML = `
        <div class="tabla-clasificacion-wrap">
            <table class="tabla-clasificacion-real">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Equipo</th>
                        <th title="Partidos Jugados">PJ</th>
                        <th title="Puntos">Pts</th>
                        <th title="Goles a Favor">GF</th>
                        <th title="Goles en Contra">GC</th>
                        <th title="Diferencia de Goles">DG</th>
                    </tr>
                </thead>
                <tbody>
                    ${data.map((e, i) => `
                        <tr class="${i < 3 ? 'top-' + (i+1) : ''}">
                            <td class="pos-num">${i + 1}</td>
                            <td class="equipo-nombre">
                                <span class="equipo-icono">⚽</span>${e.nombre}
                            </td>
                            <td>${e.partidosJugados}</td>
                            <td class="puntos-td">${e.puntos}</td>
                            <td>${e.golesAFavor}</td>
                            <td>${e.golesEnContra}</td>
                            <td class="${e.diferenciaDeGoles >= 0 ? 'dg-pos' : 'dg-neg'}">
                                ${e.diferenciaDeGoles >= 0 ? '+' : ''}${e.diferenciaDeGoles}
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>`;
    } catch (err) {
        wrap.innerHTML = '<div class="cargando-inicio">Error al cargar la clasificación.</div>';
    }
}

// ─── Partidos ───────────────────────────────────────────────────────────────
async function cargarPartidos() {
    const wrap = document.getElementById('lista-partidos-wrap');
    try {
        const res     = await fetch('/partidos');
        const partidos = await res.json();

        if (partidos.length === 0) {
            wrap.innerHTML = '<div class="cargando-inicio">No hay partidos todavía.</div>';
            return;
        }

        // Agrupar por jornada
        const porJornada = {};
        partidos.forEach(p => {
            const j = p.jornada ?? 0;
            if (!porJornada[j]) porJornada[j] = [];
            porJornada[j].push(p);
        });

        wrap.innerHTML = Object.keys(porJornada)
            .sort((a, b) => a - b)
            .map(jornada => `
                <div class="jornada-grupo">
                    <div class="jornada-titulo">Jornada ${jornada}</div>
                    ${porJornada[jornada].map(p => {
                        const fecha = p.fecha
                            ? new Date(p.fecha).toLocaleDateString('es-ES', {
                                day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
                              })
                            : 'Sin fecha';
                        const resultado = p.jugado
                            ? `<span class="resultado-badge">${p.golesLocal} - ${p.golesVisitante}</span>`
                            : `<span class="fecha-badge">${fecha}</span>`;
                        const clickable = p.jugado ? `onclick="verEstadisticasPartido(${p.id}, '${p.equipoLocalNombre} vs ${p.equipoVisitanteNombre}')"` : '';
                        return `
                        <div class="partido-card ${p.jugado ? 'jugado' : 'pendiente'}" ${clickable}>
                            <div class="partido-equipos">
                                <span class="equipo-local">${p.equipoLocalNombre}</span>
                                <div class="partido-centro">${resultado}</div>
                                <span class="equipo-visitante">${p.equipoVisitanteNombre}</span>
                            </div>
                            ${p.jugado ? '<div class="ver-stats">Ver estadísticas →</div>' : ''}
                        </div>`;
                    }).join('')}
                </div>
            `).join('');
    } catch (err) {
        wrap.innerHTML = '<div class="cargando-inicio">Error al cargar los partidos.</div>';
    }
}

async function verEstadisticasPartido(partidoId, titulo) {
    document.getElementById('modal-partido-titulo').textContent = titulo;
    document.getElementById('modal-partido-stats').innerHTML =
        '<div class="cargando-inicio">Cargando...</div>';
    document.getElementById('modal-partido').classList.remove('hidden');

    try {
        const res   = await fetch(`/partidos/${partidoId}/estadisticas`);
        const stats = await res.json();

        if (stats.length === 0) {
            document.getElementById('modal-partido-stats').innerHTML =
                '<div class="cargando-inicio">Sin estadísticas registradas.</div>';
            return;
        }

        // Separar por equipo
        const equipos = {};
        stats.forEach(s => {
            const eq = s.equipoNombre || 'Sin equipo';
            if (!equipos[eq]) equipos[eq] = [];
            equipos[eq].push(s);
        });

        document.getElementById('modal-partido-stats').innerHTML =
            Object.entries(equipos).map(([equipo, jugadores]) => `
                <div class="stats-equipo-grupo">
                    <div class="stats-equipo-titulo">${equipo}</div>
                    ${jugadores.map(j => `
                        <div class="stats-jugador-fila">
                            <span class="stats-nombre">${j.jugadorNombre}</span>
                            <span class="stats-iconos">
                                ${'⚽'.repeat(j.goles)}
                                ${'👟'.repeat(j.asistencias)}
                                ${'🟨'.repeat(j.tarjetasAmarillas)}
                                ${'🟥'.repeat(j.tarjetasRojas)}
                                ${j.posicion === 'PORTERO' && j.paradas > 0
                                    ? `<span class="paradas-badge">${j.paradas} 🧤</span>`
                                    : ''}
                            </span>
                        </div>
                    `).join('')}
                </div>
            `).join('');
    } catch (err) {
        document.getElementById('modal-partido-stats').innerHTML =
            '<div class="cargando-inicio">Error al cargar estadísticas.</div>';
    }
}

function cerrarModalPartido() {
    document.getElementById('modal-partido').classList.add('hidden');
}

// ─── Estadísticas ────────────────────────────────────────────────────────────
async function cargarEstadisticas() {
    const wrap = document.getElementById('lista-estadisticas-wrap');
    try {
        const res = await fetch('/jugadores/estadisticas');
        estadisticasData = await res.json();
        filtrarEstadisticas('puntosFantasy');
    } catch (err) {
        wrap.innerHTML = '<div class="cargando-inicio">Error al cargar estadísticas.</div>';
    }
}

function filtrarEstadisticas(campo) {
    document.querySelectorAll('.btn-filtro').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');

    const ordenados = [...estadisticasData].sort((a, b) => b[campo] - a[campo]);
    const wrap = document.getElementById('lista-estadisticas-wrap');

    wrap.innerHTML = `
    <div class="tabla-clasificacion-wrap">
        <table class="tabla-clasificacion-real">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Jugador</th>
                    <th>Equipo</th>
                    <th>Pos</th>
                    <th>⚽</th>
                    <th>👟</th>
                    <th>🟨</th>
                    <th>🟥</th>
                    <th>🧤</th>
                    <th>PFSY</th>
                </tr>
            </thead>
            <tbody>
                ${ordenados.map((j, i) => `
                    <tr>
                        <td class="pos-num">${i + 1}</td>
                        <td class="equipo-nombre">${j.nombre}</td>
                        <td>${j.equipo ?? '—'}</td>
                        <td><span class="badge-posicion ${j.posicion}" style="font-size:0.65rem">
                            ${j.posicion?.charAt(0) ?? '—'}
                        </span></td>
                        <td>${j.goles}</td>
                        <td>${j.asistencias}</td>
                        <td>${j.tarjetasAmarillas}</td>
                        <td>${j.tarjetasRojas}</td>
                        <td>${j.posicion === 'PORTERO' ? j.paradas : '—'}</td>
                        <td class="puntos-td">${j.puntosFantasy}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    </div>`;
}

// Cargar clasificación al inicio por defecto
document.addEventListener('DOMContentLoaded', () => {
    // Esperar a que el DOMContentLoaded principal ya se haya ejecutado
    setTimeout(() => cargarClasificacion(), 100);
});